package com.csun.spotr.core.adapter_item;

import com.csun.spotr.core.Place.Builder;

public class QuestDetailItem {
	private int id;
	private int questSpotId;
	private String name;
	private String description;
	private String status;
	private double longitude;
	private double latitude;
	private String url;

	public static class Builder {

		private int id;
		private int questSpotId;

		private String name;
		private String description;

		private String status;
		private String url;

		private double longitude;
		private double latitude;

		public Builder(int id, String name, String description)
		{
			this.id = id;
			this.name = name;
			this.description = description;
		}

		public Builder(int id, double longitude, double latitude)
		{
			this.id = id;
			this.longitude = longitude;
			this.latitude = latitude;
		}

		public QuestDetailItem build()
		{
			return new QuestDetailItem(this);
		}

		public Builder questSpotId(int id) {
			this.questSpotId = id;
			return this;
		}
		
		public Builder status(String status) {
			this.status = status;
			return this;
		}
		
		public Builder url(String url) {
			this.url= url;
			return this;
		}
		
		public Builder longitude(double longitude) {
			this.longitude = longitude;
			return this;
		}
		
		public Builder latitude(double latitude) {
			this.latitude= latitude;
			return this;
		}
	}
	public QuestDetailItem (int id, String name, String description, double longitude, double latitude, String status, String url) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.longitude = longitude;
		this.latitude = latitude;
		this.status = status;
		this.url = url;
	}

	public QuestDetailItem(Builder builder)
	{
		this.id = builder.id;
		this.name = builder.name;
		this.description = builder.description;
		this.status = builder.status;
		this.url = builder.url;
		this.longitude = builder.longitude;
		this.latitude = builder.latitude;
		this.questSpotId = builder.questSpotId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getQuestSpotId() {
		return questSpotId;
	}

	public void setQuestSpotId(int questSpotId) {
		this.questSpotId = questSpotId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	
	public String getTruncatedDescription() {
		if (description.length() < 50)
			return description;
		return description.substring(0, 50)+"...";
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
