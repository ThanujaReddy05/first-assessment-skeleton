/**
 * 
 */
package com.cooksys.assessment.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ftd-11
 *
 */
public class User {

	private String userName;
	private Socket port;
	private String ipAddress;
	
	private static List<User> listOfUsers = new ArrayList();
	
	/**
	 * @return the listOfUsers
	 */
	public static List<User> getListOfUsers() {
		return listOfUsers;
	}

	/**
	 * @param listOfUsers the listOfUsers to set
	 */
	public void setListOfUsers(List<User> listOfUsers) {
		this.listOfUsers = listOfUsers;
	}

	public User(String userName, Socket port, String ipAddress) {
		
		this.userName = userName;
		this.port = port;
		this.ipAddress = ipAddress;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the port
	 */
	public Socket getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(Socket port) {
		this.port = port;
	}

	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	
	
		

			
		
	

}
