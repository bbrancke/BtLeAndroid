package com.nerdlinger.btle.ui;

public class OneDeviceEvent {
	private String id;
	private String event;

	public OneDeviceEvent() { }

	public OneDeviceEvent(String Id, String Event) {
		id = Id;
		event = Event;
	}

	public String getId() {
		return id;
	}

	public void setId(String Id) {
		this.id= Id;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String Event) {
		event = Event;
	}
}
