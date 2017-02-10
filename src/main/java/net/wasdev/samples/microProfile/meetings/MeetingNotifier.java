package net.wasdev.samples.microProfile.meetings;

import javax.enterprise.context.Dependent;
import javax.websocket.server.ServerEndpoint;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.websocket.Session;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.concurrent.ArrayBlockingQueue;
import javax.websocket.OnMessage;
import javax.inject.Inject;
import java.io.IOException;
import javax.enterprise.event.Observes;

@Dependent
@ServerEndpoint("/notifier")
public class MeetingNotifier {
	@Inject
	private MeetingManager manager;
	private static ConcurrentMap<String, Queue<Session>> listeners = new ConcurrentHashMap<>();

	@OnMessage
	public void onMessage(String id, Session s) throws IOException {
		JsonObject m = manager.get(id);
		if (m == null) {
			s.close();
			return;
		}

		JsonString url = m.getJsonString("meetingURL");

		if (url != null) {
			s.getBasicRemote().sendText(url.getString());
			s.close();
			return;
		}

		Queue<Session> sessions = listeners.get(id);
		if (sessions == null) {
			sessions = new ArrayBlockingQueue<>(1000);
			Queue<Session> actual = listeners.putIfAbsent(id, sessions);
			if (actual != null) {
				sessions = actual;
			}
		}
		sessions.add(s);
	}

	public void startMeeting(@Observes @MeetingEvent MeetingStartEvent event) {
		System.out.println("hey, telling sessions");
		Queue<Session> sessions = listeners.remove(event.getId());
		if (sessions != null) {
			for (Session s : sessions) {
				if (s.isOpen()) {
					try {
						s.getBasicRemote().sendText(event.getUrl());
						s.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
