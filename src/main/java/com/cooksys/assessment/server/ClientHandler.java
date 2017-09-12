package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	private List<User> listOfUsers = new ArrayList();
	

	//TimeStamp in the specified format to display
	String timestamp = new SimpleDateFormat("MM.dd.yyyy  HH.mm.ss").format(new Date());


	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			String host = socket.getRemoteSocketAddress().toString(); // Retrieve the remote ip address connect to other end of the socket  

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);

				switch (message.getCommand()) {
				case "connect":
								log.info("{}: <{}>  has connected", timestamp, message.getUsername());
								//Add the connected user to the list of users
								listOfUsers.add(new User(message.getUsername(), socket, host));
								message.setContents(timestamp + ": <" + message.getUsername() + "> " +  " has connected");
								//Send connection alert to all the users in the list
								for (User u : listOfUsers) {
									sendToUser(writer, mapper, u, message);

								}
								break;

				case "disconnect":
								log.info("{}: <{}>  has disconnected", timestamp, message.getUsername() );
								message.setContents(timestamp + ": <" + message.getUsername() + "> "  + " has disconnected");
								//Send connection alert to all the users in the list
								for (User u : listOfUsers) {
									sendToUser(writer, mapper, u, message);

								}
								// Remove disconnected user from the list 
								for (User u : listOfUsers) {
									if (u.getUserName().equals(message.getUsername())) {
										listOfUsers.remove(u);
									}
								}
								socket.close(); 
								break;

				case "echo":
								log.info("{} <{}> (echo): {}", timestamp, message.getUsername(), message.getContents());
								message.setContents(timestamp + " <" + message.getUsername() + "> (echo): " + message.getContents());
								send(writer, mapper, message);

								break;

				
				case "broadcast":
								log.info("{} <{}> (all): {}", timestamp, message.getUsername(), message.getContents());
								message.setContents(timestamp + " <" + message.getUsername() + "> (all): " + message.getContents());
								//Send message to all the users in the list
								for (User u : listOfUsers) {
									sendToUser(writer, mapper, u, message);

								}
								break;

				case "users":
								String users = timestamp + ": currently connected users:";
								for (User u : listOfUsers) {
										users += " \n" + "<" + u.getUserName() + "> " ;
								}
								log.info(users);
								message.setContents(users);
								send(writer, mapper, message);

								break;
					
						// @username (whisper)
				default : 
								boolean userFound = false;
								if (message.getCommand().charAt(0) == '@') {
									//Search for the given username in the list
									//If found, send direct message to the user 
										for (User u : listOfUsers) {
												if (u.getUserName().equals(message.getCommand().substring(1))) {
														log.info("{} <{}> (whisper): {}", timestamp, message.getUsername(), message.getContents());
														message.setContents(timestamp + " <" + message.getUsername() + "> (whisper): " + message.getContents());
														sendToUser(writer, mapper, u, message);
														userFound = true;

												}
										}

										if(!userFound){
												message.setContents("user not found");
												send(writer, mapper, message);

										}


								}

				}
			}

		} catch (IOException e) {
			log.error("Something went wrong in ClientHandler.java :/", e);
		}
	}
	
	
	public void sendToUser(PrintWriter writer, ObjectMapper mapper, User u, Message m)
	{
		
		try {
		writer = new PrintWriter(new OutputStreamWriter(u.getPort().getOutputStream()));
		writer.write(mapper.writeValueAsString(m));
		writer.flush()	;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void send(PrintWriter writer, ObjectMapper mapper,  Message m)
	{
		
		try {
		writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		writer.write(mapper.writeValueAsString(m));
		writer.flush();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}





