package edu.regis.merc.view;

import edu.regis.merc.model.Account;
import edu.regis.merc.model.Student;
import edu.regis.merc.model.TutoringSession;
import edu.regis.merc.svc.ClientRequest;
import edu.regis.merc.svc.ServerRequestType;
import edu.regis.merc.svc.TutorReply;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LogoutViewTest {
    private final AtomicReference<String> error = new AtomicReference<>();
    private final AtomicBoolean returnedToSignIn = new AtomicBoolean();

    private TutoringSessionView view(Function<ClientRequest, TutorReply> requests) throws Exception {
        AtomicReference<TutoringSessionView> result = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> result.set(new TutoringSessionView(requests, () -> {
            assertTrue(SwingUtilities.isEventDispatchThread());
            returnedToSignIn.set(true);
        }, error::set)));
        TutoringSession saved = new TutoringSession(new Student(new Account("student@example.com")));
        saved.setSecurityToken("current-token");
        // Set only the session under test, avoiding unrelated problem rendering and windows.
        java.lang.reflect.Field model = TutoringSessionView.class.getDeclaredField("model");
        model.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try { model.set(result.get(), saved); } catch (IllegalAccessException ex) { throw new AssertionError(ex); }
        });
        return result.get();
    }

    @Test void logoutRunsOffEdtAndClearsSessionOnlyAfterConfirmation() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<ClientRequest> sent = new AtomicReference<>();
        AtomicReference<Thread> requestThread = new AtomicReference<>();
        TutoringSessionView view = view(request -> {
            assertFalse(SwingUtilities.isEventDispatchThread());
            requestThread.set(Thread.currentThread());
            sent.set(request);
            started.countDown();
            try { assertTrue(release.await(3, TimeUnit.SECONDS)); } catch (InterruptedException ex) { throw new AssertionError(ex); }
            return new TutorReply("LoggedOut");
        });
        SwingUtilities.invokeAndWait(view::logout);
        try {
            assertTrue(started.await(3, TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> {
                assertNotNull(view.getModel());
                assertEquals("current-token", view.getModel().getSecurityToken());
                assertFalse(returnedToSignIn.get());
            });
        } finally { release.countDown(); }
        await(returnedToSignIn::get);
        requestThread.get().join(1000);
        assertTrue(requestThread.get().isDaemon());
        assertFalse(requestThread.get().isAlive(), "Completed logout must not leave an idle request thread");
        SwingUtilities.invokeAndWait(() -> assertNull(view.getModel()));
        assertEquals(ServerRequestType.LOGOUT, sent.get().getRequestType());
        assertEquals("student@example.com", sent.get().getUserId());
        assertEquals("current-token", sent.get().getSecurityToken());
        assertNull(error.get());
    }

    @Test void rejectedLogoutKeepsSessionAndShowsFailure() throws Exception {
        TutoringSessionView view = view(request -> new TutorReply("InvalidToken"));
        SwingUtilities.invokeAndWait(view::logout);
        await(() -> error.get() != null);
        SwingUtilities.invokeAndWait(() -> assertNotNull(view.getModel()));
        assertFalse(returnedToSignIn.get());
    }

    @Test void connectionFailureKeepsSessionAndAllowsRetry() throws Exception {
        java.util.concurrent.atomic.AtomicInteger attempts = new java.util.concurrent.atomic.AtomicInteger();
        TutoringSessionView view = view(request -> {
            if (attempts.incrementAndGet() == 1) throw new IllegalStateException("private transport detail");
            return new TutorReply("LoggedOut");
        });
        SwingUtilities.invokeAndWait(view::logout);
        await(() -> error.get() != null);
        assertFalse(error.get().contains("private"));
        SwingUtilities.invokeAndWait(() -> assertNotNull(view.getModel()));
        assertFalse(returnedToSignIn.get());
        SwingUtilities.invokeAndWait(view::logout);
        await(returnedToSignIn::get);
        assertEquals(2, attempts.get());
    }

    private void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (System.nanoTime() < deadline) {
            AtomicBoolean done = new AtomicBoolean();
            SwingUtilities.invokeAndWait(() -> done.set(condition.getAsBoolean()));
            if (done.get()) return;
            Thread.sleep(10);
        }
        fail("Timed out waiting for logout response");
    }
}
