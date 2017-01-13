package net.wasdev.samples.microProfile.meetings;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

public class MeetingsUtil {
    public static JsonObjectBuilder createJsonFrom(JsonObject user, String ... ignoreKeys) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        List<String> doNotCopy = Arrays.asList(ignoreKeys);
        
        for (Map.Entry<String, JsonValue> entry : user.entrySet()) {
            if (!!!doNotCopy.contains(entry.getKey())) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        
        return builder;
    }
}
