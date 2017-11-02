//package broadcast;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import java.net.UnknownHostException;


public class User extends Thread {

	// The user socket
	private static Socket userSocket = null;
	// The output stream
	private static PrintStream output_stream = null;
	// The input stream
	private static BufferedReader input_stream = null;

	private static BufferedReader inputLine = null;
	private static boolean closed = false;

	public static void main(String[] args) {

		// The default port.
		int portNumber = 58888;
		// The default host.
		String host = "localhost";

		if (args.length < 2) {
			System.out
			.println("Usage: java User <host> <portNumber>\n"
					+ "Now using host=" + host + ", portNumber=" + portNumber);
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
		}

		/*
		 * Open a socket on a given host and port. Open input and output streams.
		 */
		try {
			userSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			output_stream = new PrintStream(userSocket.getOutputStream());
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to the host "
					+ host);
		}

		/*
		 * If everything has been initialized then we want to write some data to the
		 * socket we have opened a connection to on port portNumber.
		 */
		if (userSocket != null && output_stream != null && input_stream != null) {
			try {                
				/* Create a thread to read from the server. */
				new Thread(new User()).start();

				// Get user name and join the social net

				while (!closed) {
					String userMessage = new String();
					String userInput = inputLine.readLine().trim();
					// Read user input and send protocol message to server
					if (userInput.startsWith("@connect")){
						String uname = userInput.split(" ")[1];
						output_stream.println("#friendme " + uname);
					}
					else if (userInput.startsWith("@friend")){
						String uname = userInput.split(" ")[1];
						output_stream.println("#friends " + uname);
					}
					else if(userInput.startsWith("Exit")){
						output_stream.println("#Bye");
					}
					else if (userInput.startsWith("@deny")){
						String uname = userInput.split(" ")[1];
						output_stream.println("#FriendRequestDenied " + uname);
					}
					else if (userInput.startsWith("@disconnect")){
						String uname = userInput.split(" ")[1];
						output_stream.println("#unfriend " + uname);
					}


					else{
						output_stream.println(userInput);
					}

				}
				/*
				 * Close the output stream, close the input stream, close the socket.
				 */
				output_stream.close();
				input_stream.close();
				userSocket.close();
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}

	/*
	 * Create a thread to read from the server.
	 */
	public void run() {
		/*
		 * Keep on reading from the socket till we receive a Bye from the
		 * server. Once we received that then we want to break.
		 */
		String responseLine;
		
		try {
			while ((responseLine = input_stream.readLine()) != null) {
				
				if (responseLine.startsWith("#Bye")){
        			System.out.println("Bye.");
         		 	break;
        		} 
        		else if(responseLine.startsWith("#Welcome")){
        			String uname = responseLine.split(" ")[1];

        			System.out.println("Welcome "+ uname );
        		}
        		else if(responseLine.startsWith("#busy")){
        		 	System.out.println("Busy!!!");
        		 	break;
        		}
        		else if(responseLine.startsWith("#Newuser")){
        			String uname = responseLine.split(" ")[1];

        			System.out.println("<"+uname+"> enter the chat room!");
        		}
        		else if(responseLine.startsWith("#Leave")){
        			String uname = responseLine.split(" ")[1];

        			System.out.println("<"+uname+" leave the chat room!");
        		}
        		else if(responseLine.startsWith("#newStatus")){
        			String inp = responseLine.substring(10,responseLine.length());

        			System.out.println(inp);
        		}
				else if(responseLine.startsWith("#friendme")){
					String name1 = responseLine.split(" ")[1];
					System.out.println(name1+ " request to be your friend");
				}
				else if(responseLine.startsWith("#OKfriends")){
					String name1 = responseLine.split(" ")[1];
					String name2 = responseLine.split(" ")[2];
					System.out.println(name1+" and "+name2+" are now friends");

				}
				else if(responseLine.startsWith("#DenyFriendRequest")){
					String name1 = responseLine.split(" ")[1];
					System.out.println(name1 + " rejected your friend request");
				}
				else if(responseLine.startsWith("#NotFriends")){
					String name1 = responseLine.split(" ")[1];
					String name2 = responseLine.split(" ")[2];
					System.out.println(name1+" and "+name2+" are are no longer friends");
				}
				else{
					System.out.println(responseLine);
				}
				


			}
			closed = true;
			
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}
}



