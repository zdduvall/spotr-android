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
	}
	
	/* Reason for Comment:
	 * 	Removed due to lack of time to implement a LIVE notification
	 * 
	 * Author: Edgardo A. Campos
	 * Date: March 10, 2012
	 *  
	 *  WHAT WAS REMOVED
	 *  
	 *  private int type;
	 *  public FriendRequestItem(int friendId, String friendName, String message, String time, int type)
	 *  {	this.friendId = friendId;	this.friendName = friendName;	this.message = message;
	 *	    this.time = time;	this.type = type;	}
	 *	
	 *	public int getType()
	 *	{	return type;	}
	 */
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
