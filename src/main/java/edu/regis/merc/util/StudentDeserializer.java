package edu.regis.merc.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import edu.regis.merc.model.Account;
import edu.regis.merc.model.Student;
import edu.regis.merc.model.StudentModel;
import java.lang.reflect.Type;

/**
 * Restores students through their constructor so the final account field does
 * not need to be assigned through reflection.
 */
public class StudentDeserializer implements JsonDeserializer<Student> {
    @Override
    public Student deserialize(JsonElement json, Type type,
            JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Expected a student object");
        }

        JsonObject object = json.getAsJsonObject();
        Account account = context.deserialize(object.get("account"), Account.class);
        if (account == null) {
            throw new JsonParseException("Student account is required");
        }

        Student student = new Student(account);
        if (object.has("studentModel")) {
            student.setStudentModel(context.deserialize(object.get("studentModel"), StudentModel.class));
        }
        return student;
    }
}
