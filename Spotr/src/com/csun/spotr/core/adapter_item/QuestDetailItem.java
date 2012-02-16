package com.csun.spotr.core.adapter_item;

public class QuestDetailItem {
	private final int id;
	private final String name;
	private final String description;
	private String status;
	
	public QuestDetailItem (int id, String name, String description, String status) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.status = status;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getStatus(){
		return status;
	}
}
