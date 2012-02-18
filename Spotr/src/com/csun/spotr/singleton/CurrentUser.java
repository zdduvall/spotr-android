package com.csun.spotr.singleton;

import com.csun.spotr.core.User;

public class CurrentUser {
	private static final String	TAG = "(CurrentUser)";
	private static User	user;
	private static int rank = 0;
	
	public static synchronized void setCurrentUser(int id, String username, String password) {
		user = new User.Builder(id, username, password).build();
	}
	
	public static User getCurrentUser() {
		return user;
	}
	
	public static synchronized void setRank(int position) {
		rank = position;
	}
	
	public static int getRank() {
		return rank;
	}
}