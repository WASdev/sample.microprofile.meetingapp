package net.wasdev.samples.microProfile.meetings;

public class MeetingStartEvent {
	private String id;
	private String url;

	public MeetingStartEvent(String id, String url) {
		this.id = id;
		this.url = url;
	}

	public String getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}
}
