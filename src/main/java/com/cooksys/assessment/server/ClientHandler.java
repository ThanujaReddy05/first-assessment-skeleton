package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	
	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			String userIpAddress = socket.getInetAddress().toString().substring(socket.getRemoteSocketAddress().toString().indexOf("/") + 1);
			
			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				
				
				switch (message.getCommand()) {
					case "connect":
						
						log.info("{}: <{}>  has connected", timestamp, message.getUsername());
						
						User.getListOfUsers().add(new User(message.getUsername(), socket, userIpAddress));
						message.setContents(timestamp + ": <" + message.getUsername() + "> " +  " has connected");
						
						for (User u : User.getListOfUsers()) {
							writer = new PrintWriter(new OutputStreamWriter(u.getPort().getOutputStream()));
							writer.write(mapper.writeValueAsString(message));
							writer.flush();
						}
//						socket.close();
						break;
						
					case "disconnect":
						log.info("{}: <{}>  has disconnected", timestamp, message.getUsername() );
						message.setContents(timestamp + ": <" + message.getUsername() + "> "  + " has disconnected");
						
						for (User u : User.getListOfUsers()) {
							writer = new PrintWriter(new OutputStreamWriter(u.getPort().getOutputStream()));
							writer.write(mapper.writeValueAsString(message));
							writer.flush();
						}
						
						// Remove the disconnected user from  list of users
						for (User u : User.getListOfUsers()) {
							if (u.getUserName().equals(message.getUsername())) {
								User.getListOfUsers().remove(u);
							}
						}
						
						socket.close();
						
						break;
						
					case "echo":
						log.info("{} <{}> (echo): {}", timestamp, message.getUsername(), message.getContents());
						message.setContents(timestamp + " <" + message.getUsername() + "> (echo): " + message.getContents());
						writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
						writer.write(mapper.writeValueAsString(message));
						writer.flush();
//						socket.close();
						
						break;
						
					case "broadcast":
						log.info("{} <{}> (all): {}", timestamp, message.getUsername(), message.getContents());
						message.setContents(timestamp + " <" + message.getUsername() + "> (all): " + message.getContents());
						
						for (User u : User.getListOfUsers()) {
							writer = new PrintWriter(new OutputStreamWriter(u.getPort().getOutputStream()));
							writer.write(mapper.writeValueAsString(message));
							writer.flush();
						}
						
						break;
						
					case "users":
						String stringOfUsers = timestamp + ": currently connected users:";
						
						for (User u : User.getListOfUsers()) {
							stringOfUsers += " \n" + "<" + u.getUserName() + "> " ;
						}
						
						log.info(stringOfUsers);
						message.setContents(stringOfUsers);
						writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
						writer.write(mapper.writeValueAsString(message));
						writer.flush();
//						socket.close();
						break;
					
						// Direct message (whisper)
					default : 
							if (message.getCommand().charAt(0) == '@') {
								for (User u : User.getListOfUsers()) {
									if (u.getUserName().equals(message.getCommand().substring(1))) {
										log.info("{} <{}> (whisper): {}", timestamp, message.getUsername(), message.getContents());
										message.setContents(timestamp + " <" + message.getUsername() + "> (whisper): " + message.getContents());
										writer = new PrintWriter(new OutputStreamWriter(u.getPort().getOutputStream()));
									}
									else
										{
											message.setContents("User not found");
											writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
										}
									
									writer.write(mapper.writeValueAsString(message));
									writer.flush();
								}
							}
							else 
							{
								message.setContents("Not a valid command");
								writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
							}
						
						writer.write(mapper.writeValueAsString(message));
						writer.flush();
					
						
						
					}
				}
			
			
		} catch (IOException e) {
			log.error("Something went wrong in ClientHandler.java :/", e);
		}
	}
	
	
}





