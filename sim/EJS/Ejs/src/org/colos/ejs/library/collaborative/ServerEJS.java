/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Server which listens to connections Ejs
 * This class is a Thread which accept connection through a ServerSocket
 */ 

public class ServerEJS extends Thread{

	private static int port;
	private ServerSocket socketServer;
	private boolean active = true;
	private Socket socket = null;
	private SessionEJS mySession = null;

	
	/**
	* Constructor with parameters
	* @param session SessionEJS Session that manages the virtual class
	* @param int portP Port opened in the connection
	*/
	public ServerEJS(SessionEJS session,int portP) 
	{
		this.mySession = session;
		port = portP;
		//Open the Socket Server
		try{
			socketServer = new ServerSocket(port);
			this.mySession.getMyList().setText("Openning Server Socket...");
		} catch (IOException e1) {}
		this.start();
	}	
	
	
	/**
	* Method run. Overload of Thread run method. Get the students connected
	*/
	public void run()
	{
		//Beginning of the listening
		System.out.println("Executing the Server...");
		ConnectionEJS aux;
		while(this.active)
		{
			if(mySession.counter<SessionEJS.MAXCUSTOMER)
			{
				try{
					//Accept a new client connection
					socket = socketServer.accept();
					//System.out.println("Socket accepted "+socket.getPort());
					//CJB110308
					System.out.println("Accepted Socket "+socket.getLocalPort());
					//CJB110308
					//Creation of a new connection
					aux = new ConnectionEJS(socket);
					mySession.newConnectionSession(aux);
					aux.connectionOutput().writeObject(new DataSocket(null,null,"Can entry",null));
				}catch (IOException e) {}	
			}
			else
			{
				try{
					socket = socketServer.accept();
					aux = new ConnectionEJS(socket);
					aux.connectionOutput().writeObject(new DataSocket(null,null,"Can not entry",null));
					aux.closeConnection();
				} catch (IOException e) {}
			}
		}
	}
	
	/**
	* Disconnect the serverEjs
	*/
	protected void disconnect(){
		//Close the socket server
		mySession.closeSession();
		mySession.getMyList().setText("Close Socket Server...");
		active = false;	
		try {
			socketServer.close();
		} catch (IOException e) {e.printStackTrace();}
	}
}
