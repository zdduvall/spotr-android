package com.csun.spotr.core.adapter_item;

public class LootItem {
	private final int id;
	private final String imageUrl;
	private final String name;
	private final int spotLoot;
	
	public LootItem(int spot_loot,int id, String name, String imageUrl) {
		this.id = id;
		this.imageUrl = imageUrl;
		this.name = name;
		this.spotLoot = spot_loot;
	}
	
	public int getId() {
		return id;
	}
	
	public int getSpotLoot() {
		return spotLoot;
	}

	public String getImageUrl() {
		return imageUrl;
	}
	
	public String getName() {
		return name;
	}
}
