package com.csun.spotr.core;

public class Event {
	private int id;
	private String name;
	private String context;
	private String url;
	private String time;

	public Event(int id, String name, String context, String url, String time) {
		this.id = id;
		this.name = name;
		this.context = context;
		this.url = url;
		this.time = time;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
