package edu.regis.merc.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import edu.regis.merc.model.Timeout;
import java.lang.reflect.Type;

/** Restores immutable timeout fields through the constructor. */
public class TimeoutDeserializer implements JsonDeserializer<Timeout> {
    @Override
    public Timeout deserialize(JsonElement json, Type type,
            JsonDeserializationContext context) throws JsonParseException {
        TimeoutData data = context.deserialize(json, TimeoutData.class);
        return new Timeout(data.type, data.seconds, data.event, data.text);
    }

    private static class TimeoutData {
        String type;
        int seconds;
        String event;
        String text;
    }
}
