package edu.regis.merc.view;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.regis.merc.model.SecurityQuestion;
import edu.regis.merc.model.SecurityQuestionPrompt;
import edu.regis.merc.svc.ClientRequest;
import edu.regis.merc.svc.ServerRequestType;
import edu.regis.merc.svc.TutorReply;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ForgotPasswordPanelTest {
    private final Gson gson = new Gson();

    @Test
    void resizingKeepsFormFieldsAndButtonsAtNaturalHeight() throws Exception {
        ForgotPasswordPanel panel = panel(request -> null, new AtomicReference<>());
        onEdt(() -> {
            for (java.awt.Dimension size : new java.awt.Dimension[] {
                    new java.awt.Dimension(875, 650), new java.awt.Dimension(1200, 900) }) {
                panel.setSize(size);
                layout(panel);
                assertEquals(panel.userId.getPreferredSize().height, panel.userId.getHeight());
                assertEquals(panel.secAnswer.getPreferredSize().height, panel.secAnswer.getHeight());
                assertEquals(panel.lookupBut.getPreferredSize().height, panel.lookupBut.getHeight());
                assertEquals(panel.verifyBut.getPreferredSize().height, panel.verifyBut.getHeight());
                assertEquals(new java.awt.Color(0, 43, 73), panel.getBackground());
            }
        });
    }

    private void layout(java.awt.Container parent) {
        parent.doLayout();
        for (java.awt.Component child : parent.getComponents()) {
            if (child instanceof java.awt.Container container) layout(container);
        }
    }

    @Test
    void lookupDisplaysReadOnlyQuestionAndVerificationSendsOnlyEmailAndAnswer() throws Exception {
        AtomicReference<ClientRequest> sentAnswer = new AtomicReference<>();
        AtomicReference<String> destination = new AtomicReference<>();
        ForgotPasswordPanel panel = panel(request -> {
            assertFalse(SwingUtilities.isEventDispatchThread());
            if (request.getRequestType() == ServerRequestType.GET_SECURITY_QUESTION) {
                JsonObject lookup = gson.fromJson(request.getData(), JsonObject.class);
                assertEquals(1, lookup.size());
                assertEquals("student@example.com", lookup.get("userId").getAsString());
                return question(SecurityQuestion.STREET_AT_TEN);
            }
            sentAnswer.set(request);
            return new TutorReply("Verified", "\"test-reset-token\"");
        }, destination);
        lookup(panel);
        await(() -> panel.secAnswer.isEnabled());
        onEdt(() -> {
            assertFalse(panel.questionText.isEditable());
            assertEquals(SecurityQuestion.STREET_AT_TEN.toString(), panel.questionText.getText());
            panel.secAnswer.setText("Oak Street");
            panel.verifyBut.doClick();
        });
        await(() -> destination.get() != null);
        assertEquals("student@example.com:test-reset-token", destination.get());
        JsonObject answer = gson.fromJson(sentAnswer.get().getData(), JsonObject.class);
        assertEquals(2, answer.size());
        assertEquals("student@example.com", answer.get("userId").getAsString());
        assertEquals(ForgotPasswordPanel.encryptSHA256("Oak Street"), answer.get("securityAnswer").getAsString());
        onEdt(() -> assertEquals("", panel.questionText.getText()));
    }

    @Test
    void editingEmailClearsQuestionAndAnswerAndRequiresNewLookup() throws Exception {
        ForgotPasswordPanel panel = panel(request -> question(SecurityQuestion.FIRST_CAR_MAKE), new AtomicReference<>());
        lookup(panel);
        await(() -> panel.secAnswer.isEnabled());
        onEdt(() -> {
            panel.secAnswer.setText("Ford");
            panel.userId.setText("other@example.com");
            assertEquals("", panel.questionText.getText());
            assertEquals(0, panel.secAnswer.getPassword().length);
            assertFalse(panel.secAnswer.isEnabled());
            assertFalse(panel.verifyBut.isEnabled());
            assertTrue(panel.lookupBut.isEnabled());
        });
    }

    @Test
    void staleLookupCannotReplaceQuestionForNewEmail() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ForgotPasswordPanel panel = panel(request -> {
            if (request.getData().contains("student@example.com")) {
                started.countDown();
                try {
                    if (!release.await(3, TimeUnit.SECONDS)) throw new IllegalStateException("Test timeout");
                } catch (InterruptedException ex) { throw new IllegalStateException(ex); }
                return question(SecurityQuestion.BIRTH_CITY);
            }
            return question(SecurityQuestion.STREET_AT_TEN);
        }, new AtomicReference<>());
        lookup(panel);
        assertTrue(started.await(3, TimeUnit.SECONDS));
        try {
            onEdt(() -> {
                panel.userId.setText("other@example.com");
                panel.lookupBut.doClick();
            });
            await(() -> panel.secAnswer.isEnabled());
        } finally { release.countDown(); }
        // SwingWorker schedules done() on the EDT after the background work returns.
        Thread.sleep(150);
        onEdt(() -> assertEquals(SecurityQuestion.STREET_AT_TEN.toString(), panel.questionText.getText()));
    }

    @Test
    void incorrectAnswerDoesNotOpenResetAndCanBeRetried() throws Exception {
        AtomicReference<String> destination = new AtomicReference<>();
        ForgotPasswordPanel panel = panel(request -> request.getRequestType() == ServerRequestType.GET_SECURITY_QUESTION
                ? question(SecurityQuestion.MOTHERS_MAIDEN_NAME) : new TutorReply("InvalidAnswer"), destination);
        lookup(panel);
        await(() -> panel.secAnswer.isEnabled());
        onEdt(() -> { panel.secAnswer.setText("wrong"); panel.verifyBut.doClick(); });
        await(() -> panel.msg.getText().contains("did not match"));
        assertNull(destination.get());
        onEdt(() -> {
            assertEquals(0, panel.secAnswer.getPassword().length);
            assertTrue(panel.secAnswer.isEnabled());
            assertFalse(panel.verifyBut.isEnabled());
        });
    }

    @Test
    void unavailableAndMalformedRepliesKeepAnswerDisabled() throws Exception {
        for (TutorReply reply : new TutorReply[] { new TutorReply("RecoveryUnavailable"), null,
                new TutorReply("SecurityQuestion", "{}") }) {
            ForgotPasswordPanel panel = panel(request -> reply, new AtomicReference<>());
            lookup(panel);
            await(() -> panel.lookupBut.isEnabled());
            onEdt(() -> {
                assertFalse(panel.secAnswer.isEnabled());
                assertFalse(panel.verifyBut.isEnabled());
                assertEquals("", panel.questionText.getText());
                assertTrue(panel.msg.getText().startsWith("Unable"));
            });
        }
    }

    private TutorReply question(SecurityQuestion question) {
        return new TutorReply("SecurityQuestion", gson.toJson(new SecurityQuestionPrompt(question)));
    }

    @Test
    void backClearsRecoveryState() throws Exception {
        ForgotPasswordPanel panel = panel(request -> question(SecurityQuestion.BIRTH_CITY), new AtomicReference<>());
        lookup(panel);
        await(() -> panel.secAnswer.isEnabled());
        onEdt(() -> {
            panel.secAnswer.setText("Denver");
            panel.backBut.doClick();
            assertEquals("", panel.userId.getText());
            assertEquals("", panel.questionText.getText());
            assertEquals(0, panel.secAnswer.getPassword().length);
            assertFalse(panel.verifyBut.isEnabled());
            assertFalse(panel.secAnswer.isEnabled());
        });
    }

    @Test
    void connectionFailureAllowsLookupRetry() throws Exception {
        ForgotPasswordPanel panel = panel(request -> { throw new IllegalStateException("offline"); },
                new AtomicReference<>());
        lookup(panel);
        await(() -> panel.lookupBut.isEnabled());
        onEdt(() -> {
            assertTrue(panel.msg.getText().startsWith("Unable"));
            assertFalse(panel.verifyBut.isEnabled());
        });
    }

    private ForgotPasswordPanel panel(Function<ClientRequest, TutorReply> requests,
            AtomicReference<String> destination) throws Exception {
        AtomicReference<ForgotPasswordPanel> panel = new AtomicReference<>();
        onEdt(() -> panel.set(new ForgotPasswordPanel(requests,
                (email, token) -> destination.set(email + ":" + token), () -> {})));
        return panel.get();
    }

    private void lookup(ForgotPasswordPanel panel) throws Exception {
        onEdt(() -> {
            assertFalse(panel.verifyBut.isEnabled());
            panel.userId.setText("student@example.com");
            panel.lookupBut.doClick();
        });
    }

    private void onEdt(Runnable action) throws Exception { SwingUtilities.invokeAndWait(action); }

    private void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            AtomicReference<Boolean> done = new AtomicReference<>();
            onEdt(() -> done.set(condition.getAsBoolean()));
            if (done.get()) return;
            Thread.sleep(10);
        }
        fail("Timed out waiting for UI response");
    }
}
