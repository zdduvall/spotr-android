package com.csun.spotr.core;

public class Challenge {
	static public enum Type {
		CHECK_IN, 
		SNAP_PICTURE, 
		WRITE_ON_WALL, 
		QUESTION_ANSWER,
		SNAP_PICTURE_CHALLENGE,
		FIND_TREASURE,
		DROP_ITEM,
		OTHER
	};
	
	public static Type returnType(String typeStr) {
		if (typeStr.equals("CHECK_IN"))
			return Type.CHECK_IN;
		else if (typeStr.equals("SNAP_PICTURE"))
			return Type.SNAP_PICTURE;
		else if (typeStr.equals("WRITE_ON_WALL"))
			return Type.WRITE_ON_WALL;
		else if (typeStr.equals("QUESTION_ANSWER"))
			return Type.QUESTION_ANSWER;
		else if (typeStr.equals("SNAP_PICTURE_CHALLENGE"))
			return Type.SNAP_PICTURE_CHALLENGE;
		else if (typeStr.equals("TREASURE")) 
			return Type.FIND_TREASURE;
		else if (typeStr.equals("DROP_ITEM"))
			return Type.DROP_ITEM;
		else // (typeStr.equals("OTHER")) 
			return Type.OTHER;
	}
	
	private final int id;
	private final Type type;
	private final int points;
	
	private String name;
	private String description;
	private int rating;
	private String status;
	
	public static class Builder {
		private final int id;
		private final Type type;
		private final int points;
		
		private String name;
		private String description;
		private int rating;
		private String status;
		
		public Builder(int id, Type type, int points) {
			this.id = id;
			this.type = type;
			this.points = points;
			
			name = "n/a";
			description = "n/a";
			rating = 0;
		}
	
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder description(String description) {
			this.description = description;
			return this;
		}
		
		public Builder rating(int rating) {
			this.rating = rating;
			return this;
		}
		
		public Builder status(String status){
			this.status = status;
			return this;
		}
		
		public Challenge build() {
			return new Challenge(this);
		}
	}
	
	public Challenge(Builder builder) {
		this.id = builder.id;
		this.type = builder.type;
		this.points = builder.points;
		this.name = builder.name;
		this.description = builder.description;
		this.rating = builder.rating;
		this.status = builder.status;
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

	public void setDescription(String description) {
		this.description = description;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public int getId() {
		return id;
	}

	public Type getType() {
		return type;
	}

	public int getPoints() {
		return points;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
