package edu.regis.merc.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import edu.regis.merc.model.Student;
import edu.regis.merc.model.TutoringSession;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StudentDeserializerTest {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Student.class, new StudentDeserializer())
            .create();

    @Test
    void restoresStudentInsideSessionIncludingSavedModel() {
        String json = """
                {"student":{"account":{"userId":"student@example.com","firstName":"Pat"},
                  "studentModel":{"userId":"student@example.com","assessments":{
                    "7":{"exposures":3}},"scaffoldLevel":"EXTREME"}}}
                """;
        TutoringSession session = gson.fromJson(json, TutoringSession.class);
        Student student = session.getStudent();

        assertEquals("student@example.com", student.getAccount().getUserId());
        assertEquals("Pat", student.getAccount().getFirstName());
        assertEquals("student@example.com", student.getStudentModel().getUserId());
        assertTrue(student.getStudentModel().containsAssessment(7));
        assertEquals(3, student.getStudentModel().findAssessment(7).getExposures());
    }

    @Test
    void missingModelKeepsConstructorDefaults() {
        Student student = gson.fromJson(
                "{\"account\":{\"userId\":\"student@example.com\"}}", Student.class);
        assertEquals("student@example.com", student.getStudentModel().getUserId());
        assertTrue(student.getStudentModel().getAssessments().isEmpty());
    }

    @Test
    void rejectsMissingOrNullAccount() {
        assertThrows(JsonParseException.class, () -> gson.fromJson("{}", Student.class));
        assertThrows(JsonParseException.class,
                () -> gson.fromJson("{\"account\":null}", Student.class));
    }
}
