package net.wasdev.samples.microProfile.meetings;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MeetingsUtil {
    public static JsonObject meetingAsJsonObject(DBObject obj) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("id", (String) obj.get("_id"));
        String meetingURL = (String) obj.get("meetingURL");
        if (meetingURL != null)
            builder.add("meetingURL", meetingURL);
        builder.add("title", (String) obj.get("title"));
        builder.add("duration", (Long) obj.get("duration"));

        return builder.build();
    }

    public static DBObject meetingAsMongo(JsonObject json, DBObject mongo) {
        if (mongo == null) {
            mongo = new BasicDBObject();
            mongo.put("_id", json.getString("id"));
        }
        mongo.put("title", json.getString("title"));
        mongo.put("duration", ((JsonNumber) json.get("duration")).longValue());
        JsonString jsonString = json.getJsonString("meetingURL");
        if (jsonString != null) {
            mongo.put("meetingURL", jsonString.getString());
        }
        return mongo;
    }

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
