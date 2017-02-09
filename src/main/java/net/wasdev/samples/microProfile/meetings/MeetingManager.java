package net.wasdev.samples.microProfile.meetings;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@ApplicationScoped
public class MeetingManager {
	@Resource(name = "mongo/sampledb")
	private DB meetings;

	public DBCollection getColl() {
		return meetings.getCollection("meetings");
	}

	public void add(JsonObject meeting) {
		DBCollection coll = getColl();
		DBObject existing = coll.findOne(meeting.getString("id"));
		DBObject obj = MeetingsUtil.meetingAsMongo(meeting, existing);
		coll.save(obj);

	}

	public JsonObject get(String id) {
		return MeetingsUtil.meetingAsJsonObject(getColl().findOne(id));
	}

	public JsonArray list() {
		JsonArrayBuilder results = Json.createArrayBuilder();

		for (DBObject meeting : getColl().find()) {
			results.add(MeetingsUtil.meetingAsJsonObject(meeting));
		}

		return results.build();
	}

	public void startMeeting(JsonObject meeting) {
		String id = meeting.getString("id");
		String url = meeting.getString("meetingURL");
		DBCollection coll = getColl();
		DBObject obj = coll.findOne(id);
		obj.put("meetingURL", url);
		coll.save(obj);
	}
}
