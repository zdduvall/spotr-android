package com.csun.spotr.singleton;

import com.csun.spotr.core.User;

public class CurrentUser {
	private static final String	TAG = "(CurrentUser)";
	private static User	user;
	private static int rank = 0;
	
	/*
	 * Power up features
	 */
	private static double 	powerUpBounus;       // increase points by points * powerUpBous + points
	private static double   powerUpLoan;		 // add points to current points 
	private static double 	powerUpTelescope; 	 // increase radius when listen for places
	private static boolean  powerUpTeleport;	 // if true, user can choose a point in map to listen for places
	private static boolean  powerUpSneak;		 // if true, user can see the treasure before decide to open it
	private static boolean  powerUpLuck;		 // if true, user will have 50% chances to get 2 treasures 
	
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
	
	public static synchronized double getPowerUpBounus() {
		return powerUpBounus;
	}

	public static synchronized void setPowerUpBounus(double bonus) {
		powerUpBounus = bonus;
	}

	public static synchronized double getPowerUpLoan() {
		return powerUpLoan;
	}

	public static synchronized void setPowerUpLoan(double loan) {
		powerUpLoan = loan;
	}

	public static synchronized double getPowerUpTelescope() {
		return powerUpTelescope;
	}

	public static synchronized void setPowerUpTelescope(double telescope) {
		powerUpTelescope = telescope;
	}

	public static synchronized boolean isPowerUpTeleport() {
		return powerUpTeleport;
	}

	public static synchronized void setPowerUpTeleport(boolean yes) {
		powerUpTeleport = yes;
	}

	public static synchronized boolean isPowerUpSneak() {
		return powerUpSneak;
	}

	public static synchronized void setPowerUpSneak(boolean yes) {
		powerUpSneak = yes;
	}

	public static synchronized boolean isPowerUpLuck() {
		return powerUpLuck;
	}

	public static synchronized void setPowerUpLuck(boolean yes) {
		powerUpLuck = yes;
	}
}