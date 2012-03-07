package com.csun.spotr.core;

public class Badge {
	public static final int BADGE_100_POINTS 		  = 1;
	public static final int BADGE_1000_POINTS 		  = 2;
	public static final int BADGE_10000_POINTS        = 3;
	public static final int BADGE_FIRST_CHECK_IN 	  = 4;
	public static final int BADGE_FIRST_QUEST 		  = 5;
	public static final int BADGE_NEWBIE 			  = 6;
	public static final int BADGE_TRAVELER 		      = 7;
	public static final int BADGE_EXPLORER 		      = 8;
	public static final int BADGE_ADVENTURE 		  = 9;
	public static final int BADGE_POWER_ATHELETE      = 10;
	public static final int BADGE_BOOK_WORM 		  = 11;
	public static final int BADGE_COFFEE_AFICIANADO   = 12;
	public static final int BADGE_MR_PHOTOGRAPHER     = 13;
	public static final int BADGE_NIGHT_OWL 		  = 14;
	public static final int BADGE_JAVA_LEGENDRY 	  = 15;
	public static final int BADGE_CPP_LEGENDRY 	      = 16;
	public static final int BADGE_CSHARP_LEGENDRY 	  = 17;
	public static final int BADGE_POPULAR_FEED 	      = 18;
	public static final int BADGE_FAMOUS_FEED 	      = 19;
	public static final int BADGE_NOTORIOUS_FEED      = 20;
	public static final int BADGE_ADDICT 			  = 21;
	public static final int BADGE_BEST_REWARD         = 22;
	
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
