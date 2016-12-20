package net.wasdev.samples.microProfile.meetings;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
 
import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.Json;
import javax.json.JsonValue;
import javax.json.JsonObjectBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MeetingManager {
	private ConcurrentMap<String, JsonObject> meetings = new ConcurrentHashMap<>();

	public void add(JsonObject meeting) {
	    meetings.putIfAbsent(meeting.getString("id"), meeting);
	}
	public JsonObject get(String id) {
	    return meetings.get(id);
	}
	public JsonArray list() {
	    JsonArrayBuilder results = Json.createArrayBuilder();
	     
	    for (JsonObject meeting : meetings.values()) {
	        results.add(meeting);
	    }
	         
	    return results.build();
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
	public void startMeeting(JsonObject meeting) {
	    String id = meeting.getString("id");
	    String url = meeting.getString("meetingURL");
	    JsonObject existingMeeting = meetings.get(id);
	    JsonObject updatedMeeting = MeetingsUtil.createJsonFrom(existingMeeting).add("meetingURL", url).build();
	    meetings.replace(id, existingMeeting, updatedMeeting);
	}
}
