package com.csun.spotr.core;

import java.util.HashSet;
import java.util.Set;

public class Place {
	private final double longitude;
	private final double latitude;
	private final int id;

	private String googleId;
	private String address;
	private String name;
	private String types;
	private String iconUrl;
	private String phoneNumber;
	private String websiteUrl;
	private int rating;
	private int type;
	private Set<Integer> challenges;

	public static class Builder {
		// required parameter
		private final double longitude;
		private final double latitude;
		private final int id;

		// optional parameter
		private String googleId = "n/a";
		private String address = "n/a";
		private String name = "n/a";
		private String iconUrl = "n/a";
		private String phoneNumber = "(888) 888-8888";
		private String websiteUrl = "http://www.google.com/";
		private int type = 0;
		private Set<Integer> challenges = new HashSet<Integer>();
		private int rating = 0;

		public Builder(double longitude, double latitude, int id) {
			assert (longitude >= -180.0 && longitude <= 180.0);
			assert (latitude >= -90.0 && longitude <= 90.0);
			this.longitude = longitude;
			this.latitude = latitude;
			this.id = id;
		}

		public Builder googleId(String googleId) {
			this.googleId = googleId;
			return this;
		}

		public Builder address(String address) {
			this.address = address;
			return this;
		}

		public Builder type(int type) {
			this.type = type;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder iconUrl(String iconUrl) {
			this.iconUrl = iconUrl;
			return this;
		}

		public Builder phoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
			return this;
		}

		public Builder websiteUrl(String websiteUrl) {
			this.websiteUrl = websiteUrl;
			return this;
		}

		public Builder builder(int rating) {
			this.rating = rating;
			return this;
		}

		public Place build() {
			return new Place(this);
		}
	}

	public Place(Builder builder) {
		// required parameters
		longitude = builder.longitude;
		latitude = builder.latitude;
		id = builder.id;
		googleId = builder.googleId;

		// optional parameters
		address = builder.address;
		type = builder.type;
		name = builder.name;
		iconUrl = builder.iconUrl;
		phoneNumber = builder.phoneNumber;
		websiteUrl = builder.websiteUrl;
		rating = builder.rating;
		challenges = builder.challenges;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public int getId() {
		return id;
	}

	public String getGoogleId() {
		return googleId;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public int getType() {
		return type;
	}

	public void setTypes(String types) {
		this.types = types;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setWebsiteUrl(String websiteUrl) {
		this.websiteUrl = websiteUrl;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public int getRating() {
		return rating;
	}

	public Set<Integer> getChallenges() {
		return new HashSet<Integer>(challenges);
	}

	public void addChallenges(Integer i) {
		this.challenges.add(i);
	}

	public void showChallenges() {
		for (Integer i : challenges) {
			System.out.print(i + ", ");
		}
	}

	@Override
	public String toString() {
		return "Place [longitude=" + longitude + ", latitude=" + latitude + ", id=" + id + ", googleId=" + googleId + ", address=" + address + ", name=" + name + ", types=" + types + ", iconUrl=" + iconUrl + ", phoneNumber=" + phoneNumber + ", websiteUrl=" + websiteUrl + ", rating=" + rating + ", challenges=" + challenges + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((googleId == null) ? 0 : googleId.hashCode());
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Place other = (Place) obj;
		if (googleId == null) {
			if (other.googleId != null)
				return false;
		}
		else if (!googleId.equals(other.googleId))
			return false;
		if (id != other.id)
			return false;
		return true;
	}
}