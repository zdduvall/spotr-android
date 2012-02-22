package com.csun.spotr.core.adapter_item;

public class QuestDetailItem {
	private final int id;
	private final String name;
	private final String description;
	private String status;
	private final double longitude;
	private final double latitude;
	
	public QuestDetailItem (int id, String name, String description, double longitude, double latitude, String status) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.longitude = longitude;
		this.latitude = latitude;
		this.status = status;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
	public String getDescription() {
		return description;
	}
	
	public String getStatus(){
		return status;
	}
}
