package com.dogs.dogsitters;

import java.util.ResourceBundle;

public class ApplicationProperties {
	public static String DRIVER_SQL = "DRIVER_SQL";
	public static String CONNECTION_STRING_SQL = "CONNECTION_STRING_SQL";
	public static String USER_SQL = "USER_SQL";
	public static String PASSWORD_SQL = "PASSWORD_SQL";
	public static String PASSWORD_ADMIN = "PASSWORD_ADMIN";
	
	static ResourceBundle rb = ResourceBundle.getBundle("application");
	
	public static String getProperty(String name) {
		return rb.getString(name);
	}
}