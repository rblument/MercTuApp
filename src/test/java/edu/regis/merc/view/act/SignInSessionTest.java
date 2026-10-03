package edu.regis.merc.view.act;

import com.google.gson.Gson;
import edu.regis.merc.model.Timeout;
import edu.regis.merc.model.TutoringSession;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SignInSessionTest {
    private final Gson gson = SignInAction.createSessionGson();

    @Test
    void restoresStudentAndNestedStepTimeoutWithLoginParser() {
        String json = """
                {
                  "student":{"account":{"userId":"student@example.com"}},
                  "tasks":[{
                    "task":{"steps":[{"id":50,"timeout":{
                      "type":"Step","seconds":30,"event":"Hint","text":"Need help?"
                    }}]},
                    "currentStep":{"step":{"id":50,"timeout":{
                      "type":"Step","seconds":30,"event":"Hint","text":"Need help?"
                    }}}
                  }]
                }
                """;
        TutoringSession session = gson.fromJson(json, TutoringSession.class);
        assertEquals("student@example.com", session.getStudent().getAccount().getUserId());
        Timeout timeout = session.currentTask().currentStep().getStep().getTimeout();
        assertEquals("Step", timeout.getType());
        assertEquals(30, timeout.getSeconds());
        assertEquals("Hint", timeout.getEvent());
        assertEquals("Need help?", timeout.getText());
        assertEquals(30, session.currentTask().getTask().getSteps().get(0).getTimeout().getSeconds());
    }

    @Test
    void preservesNullAndDefaultTimeoutValues() {
        assertNull(gson.fromJson("null", Timeout.class));
        Timeout timeout = gson.fromJson("{}", Timeout.class);
        assertEquals(0, timeout.getSeconds());
        assertNull(timeout.getType());
        assertNull(timeout.getEvent());
        assertNull(timeout.getText());
    }
}
