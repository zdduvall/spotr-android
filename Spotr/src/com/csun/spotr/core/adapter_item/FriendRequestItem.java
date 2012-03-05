package com.csun.spotr.core.adapter_item;

public class FriendRequestItem {
	private int friendId;
	private String friendName;
	private String message;
	private String time;

	public FriendRequestItem(int friendId, String friendName, String message, String time) {
		this.friendId = friendId;
		this.friendName = friendName;
		this.message = message;
		this.time = time;
		this.type = 1;
	}
	/* Little test area. Adding a type variable
	 * Author: Edgardo C.
	 * Added a new int type which holds the type of the notification
	 * 		(1) Friend request	(2)	Comment	(3) Awward [currently assumed]
	 * Added a new constructor with an additional field that takes in type.
	 * Added a getType() function which returns the type of the notification.
	 */
	private int type;
	public FriendRequestItem(int friendId, String friendName, String message, String time, int type)
	{
		this.friendId = friendId;
		this.friendName = friendName;
		this.message = message;
		this.time = time;
		this.type = type;
	}
	
	public int getType()
	{
		return type;
	}
	/* end little test area */
	public int getFriendId() {
		return friendId;
	}

	public void setFriendId(int friendId) {
		this.friendId = friendId;
	}

	public String getFriendName() {
		return friendName;
	}

	@Override
	public String toString() {
		return friendName + "has sent you a request with message \"" + message + "\"";
	}

	public void setFriendName(String friendName) {
		this.friendName = friendName;
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

}
