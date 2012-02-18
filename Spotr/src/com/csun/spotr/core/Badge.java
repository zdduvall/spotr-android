package com.csun.spotr.core;

public class Badge {
	private final int id;
	private final String date;
	private final String url;
	private final String description;
	private int points;

	public Badge(int id, String name, String description, String url, String date, int points) {
		this.id = id;
		this.name = name;
		this.date = date;
		this.url = url;
		this.description = description;
		this.points = points;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	private final String name;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDate() {
		return date;
	}

	public String getUrl() {
		return url;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
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
		Badge other = (Badge) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		}
		else if (!date.equals(other.date))
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Badge [id=" + id + ", date=" + date + ", url=" + url + ", description=" + description + ", name=" + name + "]";
	}
}
