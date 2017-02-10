package net.wasdev.samples.microProfile.meetings;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@ApplicationScoped
public class MeetingManager {
	@Resource(name = "mongo/sampledb")
	private DB meetings;
	@Resource
	private ManagedScheduledExecutorService executor;
	@Inject
	@MeetingEvent
	private Event<MeetingStartEvent> events;

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
		final String id = meeting.getString("id");
		String url = meeting.getString("meetingURL");
		DBCollection coll = getColl();
		DBObject obj = coll.findOne(id);
		obj.put("meetingURL", url);
		coll.save(obj);

		long duration = ((Number) obj.get("duration")).longValue();
		TimeUnit unit = TimeUnit.MINUTES;
		executor.schedule(new Runnable() {

			@Override
			public void run() {
				DBCollection coll = getColl();
				DBObject obj = coll.findOne(id);
				obj.removeField("meetingURL");
				coll.save(obj);
				System.out.println(id + " meeting ended");
			}
		}, duration, unit);

		MeetingStartEvent eventObject = new MeetingStartEvent(id, url);
		events.fire(eventObject);
	}
}
