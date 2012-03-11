package com.csun.spotr.core;

import java.util.Date;

public class User {
	// required parameters
	private final int id;
	private final String username;
	private String password;
	// optional parameters
	private String realname;
	private String education;
	private String hometown;
	private String hobbies;
	private Date dateOfBirth;
	private int points;
	private int challengesDone;
	private int placesVisited;
	private int rank;
	private String imageUrl;
	private int numFriends;
	private int numBadges;

	public static class Builder {
		// required parameters
		private final int id;
		private final String username;
		private String password;
		// optional parameters
		private String realname;
		private String education;
		private String hometown;
		private String hobbies;
		private Date dateOfBirth;
		private int points;
		private int challengesDone;
		private int placesVisited;
		private int rank;
		private String imageUrl;
		private int numFriends;
		private int numBadges;

		public Builder(int id, String username, String password) {
			// required parameters
			this.id = id;
			this.username = username;
			this.password = password;
			// optional parameters
			realname = null;
			education = null;
			hometown = null;
			hobbies = null;
			dateOfBirth = null;
			points = 0;
			challengesDone = 0;
			placesVisited = 0;
			imageUrl = null;
		}
		
		public Builder realname(String realname) {
			this.realname = realname;
			return this;
		}
		
		public Builder education(String education) {
			this.education = education;
			return this;
		}
		
		public Builder hometown(String hometown) {
			this.hometown = hometown;
			return this;
		}
		
		public Builder hobbies(String hobbies) {
			this.hobbies = hobbies;
			return this;
		}

		public Builder dateOfBirth(Date dateOfBirDate) {
			this.dateOfBirth = dateOfBirDate;
			return this;
		}

		public Builder points(int points) {
			this.points = points;
			return this;
		}

		public Builder challengesDone(int challengesDone) {
			this.challengesDone = challengesDone;
			return this;
		}

		public Builder placesVisited(int placesVisited) {
			this.placesVisited = placesVisited;
			return this;
		}

		public Builder imageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
			return this;
		}

		public Builder rank(int rank) {
			this.rank = rank;
			return this;
		}

		public Builder numFriends(int f) {
			this.numFriends = f;
			return this;
		}

		public Builder numBadges(int b) {
			this.numBadges = b;
			return this;
		}

		public User build() {
			return new User(this);
		}
	}

	public User(Builder builder) {
		this.id = builder.id;
		this.username = builder.username;
		this.password = builder.password;
		this.realname = builder.realname;
		this.education = builder.education;
		this.hometown = builder.hometown;
		this.hobbies = builder.hobbies;
		this.dateOfBirth = builder.dateOfBirth;
		this.points = builder.points;
		this.challengesDone = builder.challengesDone;
		this.placesVisited = builder.placesVisited;
		this.rank = builder.rank;
		this.imageUrl = builder.imageUrl;
		this.numFriends = builder.numFriends;
		this.numBadges = builder.numBadges;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getEducation() {
		return education;
	}

	public void setEducation(String education) {
		this.education = education;
	}
	
	public String getHometown() {
		return hometown;
	}

	public void setHometown(String hometown) {
		this.hometown = hometown;
	}
	
	public String getHobbies() {
		return hobbies;
	}

	public void setHobbies(String hobbies) {
		this.hobbies = hobbies;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public int getChallengesDone() {
		return challengesDone;
	}

	public void setChallengesDone(int challengesDone) {
		this.challengesDone = challengesDone;
	}

	public int getPlacesVisited() {
		return placesVisited;
	}

	public void setPlacesVisited(int placesVisited) {
		this.placesVisited = placesVisited;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public int getNumFriends() {
		return numFriends;
	}

	public void setNumFriends(int numFriends) {
		this.numFriends = numFriends;
	}

	public int getNumBadges() {
		return numBadges;
	}

	public void setNumBadges(int numBadges) {
		this.numBadges = numBadges;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		User other = (User) obj;
		if (id != other.id)
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		}
		else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password=" + password + ", realname=" + realname + ", education=" + education + ", hometown=" + hometown +", hobbies=" + hobbies + ", dateOfBirth=" + dateOfBirth + ", points=" + points + ", challengesDone=" + challengesDone + ", placesVisited=" + placesVisited + ", imageUrl=" + imageUrl + "]";
	}
}
