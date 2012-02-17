package com.csun.spotr.core;

public class Inbox {
	private final int id;
	private String username;
	private String userPictureUrl;
	private String subjectLine;
	private String message;
	private String time;
	private int isNew;

	public Inbox(int id, String username, String userPictureUrl, String subjectLine, String message, String time, int isNew) {
		this.id = id;
		this.username = username;
		this.userPictureUrl = userPictureUrl;
		this.subjectLine = subjectLine;
		this.message = message;
		this.time = time;
		this.isNew = isNew;
	}
	
	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUserPictureUrl() {
		return userPictureUrl;
	}

	public void setUserPictureUrl(String userPictureUrl) {
		this.userPictureUrl = userPictureUrl;
	}

	public String getSubjectLine() {
		return subjectLine;
	}

	public void setSubjectLine(String subjectLine) {
		this.subjectLine = subjectLine;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int isNew() {
		return isNew;
	}

	public void setNew(int isNew) {
		this.isNew = isNew;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((time == null) ? 0 : time.hashCode());
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
		Inbox other = (Inbox) obj;
		if (id != other.id)
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		}
		else if (!time.equals(other.time))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Inbox [id=" + id + ", username=" + username + ", userPictureUrl=" + userPictureUrl + ", subjectLine=" + subjectLine + ", message=" + message + ", time=" + time + ", isNew=" + isNew + "]";
	}
}
