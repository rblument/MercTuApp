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
    private final AtomicBoolean exited = new AtomicBoolean();

    private TutoringSessionView view(Function<ClientRequest, TutorReply> requests) throws Exception {
        AtomicReference<TutoringSessionView> result = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> result.set(new TutoringSessionView(requests, () -> {
            assertTrue(SwingUtilities.isEventDispatchThread());
            returnedToSignIn.set(true);
        }, () -> {
            assertTrue(SwingUtilities.isEventDispatchThread());
            exited.set(true);
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
        assertFalse(exited.get(), "Logout must not exit the application");
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

    @Test void closeAndWindowXRevokeBeforeExitWithoutShowingSignIn() throws Exception {
        for (boolean windowX : new boolean[] {false, true}) {
            exited.set(false);
            AtomicReference<ClientRequest> sent = new AtomicReference<>();
            TutoringSessionView view = view(request -> {
                assertFalse(exited.get(), "Application exited before revocation");
                assertFalse(SwingUtilities.isEventDispatchThread());
                sent.set(request);
                return new TutorReply("LoggedOut");
            });
            SwingUtilities.invokeAndWait(() -> {
                if (windowX) TutoringSessionView.closeListener(view::closeSession).windowClosing(null);
                else view.closeSession();
            });
            await(exited::get);
            assertEquals(ServerRequestType.LOGOUT, sent.get().getRequestType());
            SwingUtilities.invokeAndWait(() -> assertNull(view.getModel()));
            assertFalse(returnedToSignIn.get());
        }
    }

    @Test void closeWithoutActiveSessionExitsWithoutNetwork() throws Exception {
        AtomicReference<TutoringSessionView> result = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            TutoringSessionView view = new TutoringSessionView(request -> { throw new AssertionError("No session to revoke"); },
                    () -> returnedToSignIn.set(true), () -> exited.set(true), error::set);
            result.set(view);
            view.closeSession();
        });
        assertTrue(exited.get());
        assertFalse(returnedToSignIn.get());
        assertNull(result.get().getModel());
    }

    @Test void failedCloseRetainsSessionAndCanBeRetriedOrCancelled() throws Exception {
        java.util.concurrent.atomic.AtomicInteger attempts = new java.util.concurrent.atomic.AtomicInteger();
        TutoringSessionView view = view(request -> new TutorReply(attempts.incrementAndGet() == 1 ? ":ERR" : "LoggedOut"));
        SwingUtilities.invokeAndWait(view::closeSession);
        await(() -> error.get() != null);
        assertFalse(exited.get());
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull(view.getModel());
            assertEquals("current-token", view.getModel().getSecurityToken());
            assertTrue(button(view, "Close").isEnabled());
            assertTrue(button(view, "Logout").isEnabled());
        });
        // Dismissing the error leaves the application open. A later Close retries the operation.
        SwingUtilities.invokeAndWait(view::closeSession);
        await(exited::get);
        assertEquals(2, attempts.get());
    }

    @Test void closeAndWindowXCannotInterruptOrDuplicatePendingLogout() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger attempts = new java.util.concurrent.atomic.AtomicInteger();
        TutoringSessionView view = view(request -> {
            attempts.incrementAndGet();
            started.countDown();
            try { assertTrue(release.await(3, TimeUnit.SECONDS)); } catch (InterruptedException ex) { throw new AssertionError(ex); }
            return new TutorReply("LoggedOut");
        });
        SwingUtilities.invokeAndWait(view::logout);
        try {
            assertTrue(started.await(3, TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> {
                assertFalse(button(view, "Close").isEnabled());
                assertFalse(button(view, "Logout").isEnabled());
                view.logout();
                view.closeSession();
                TutoringSessionView.closeListener(view::closeSession).windowClosing(null);
                assertNotNull(view.getModel());
                assertFalse(exited.get());
            });
        } finally { release.countDown(); }
        await(returnedToSignIn::get);
        assertEquals(1, attempts.get());
        assertFalse(exited.get());
    }

    @Test void serverProgressUpdatesDisplayedAndOwningSessionsBeforeLogout() throws Exception {
        AtomicReference<ClientRequest> sent = new AtomicReference<>();
        TutoringSessionView view = view(request -> {
            sent.set(request);
            return new TutorReply("LoggedOut");
        });
        AtomicReference<TutoringSession> owner = new AtomicReference<>();
        com.google.gson.Gson gson = edu.regis.merc.view.act.SignInAction.createSessionGson();
        String[] statuses = {"Success", "TaskComplete", "ProblemComplete", "UnitComplete"};
        for (int i = 0; i < statuses.length; i++) {
            int stepId = 51 + i;
            TutoringSession returned = new TutoringSession(new Student(new Account("student@example.com")));
            returned.setSecurityToken("server-token");
            edu.regis.merc.model.Problem problem = new edu.regis.merc.model.Problem(1);
            edu.regis.merc.model.Task task = new edu.regis.merc.model.Task(11);
            edu.regis.merc.model.Step step = new edu.regis.merc.model.Step(stepId);
            task.addStep(step);
            problem.addTask(task);
            returned.setProblem(problem);
            edu.regis.merc.model.PendingTask pending = new edu.regis.merc.model.PendingTask(task);
            pending.setCurrentStep(new edu.regis.merc.model.PendingStep(500 + i, step));
            pending.currentStep().setIsCompleted(i == statuses.length - 1);
            returned.addTask(pending);
            TutorReply reply = new TutorReply(statuses[i], gson.toJson(returned));
            SwingUtilities.invokeAndWait(() -> {
                view.setSessionListener(owner::set);
                view.applySession(reply);
                assertSame(view.getModel(), owner.get());
                assertEquals(stepId, owner.get().currentTask().currentStep().getStep().getId());
                assertEquals("server-token", owner.get().getSecurityToken());
                assertEquals(returned.currentTask().currentStep().isCompleted(),
                        owner.get().currentTask().currentStep().isCompleted());
            });
        }
        SwingUtilities.invokeAndWait(view::logout);
        await(returnedToSignIn::get);
        assertEquals("server-token", sent.get().getSecurityToken());
        assertFalse(exited.get());
    }

    private javax.swing.JButton button(java.awt.Container container, String text) {
        for (java.awt.Component child : container.getComponents()) {
            if (child instanceof javax.swing.JButton button && text.equals(button.getText())) return button;
            if (child instanceof java.awt.Container nested) {
                javax.swing.JButton found = button(nested, text);
                if (found != null) return found;
            }
        }
        return null;
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
