//package broadcast;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;



/*
 * A server that delivers status messages to other users.
 */
public class Server {

	// Create a socket for the server 
	private static ServerSocket serverSocket = null;
	// Create a socket for the server 
	private static Socket userSocket = null;
	// Maximum number of users 
	private static int maxUsersCount = 5;
	// An array of threads for users
	private static userThread[] threads = null;


	public static void main(String args[]) {

		// The default port number.
		int portNumber = 58888;
		if (args.length < 2) {
			System.out.println("Usage: java Server <portNumber>\n"
					+ "Now using port number=" + portNumber + "\n" +
					"Maximum user count=" + maxUsersCount);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
			maxUsersCount = Integer.valueOf(args[1]).intValue();
		}

		System.out.println("Server now using port number=" + portNumber + "\n" + "Maximum user count=" + maxUsersCount);
		
		
		userThread[] threads = new userThread[maxUsersCount];


		/*
		 * Open a server socket on the portNumber (default 8000). 
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 * Create a user socket for each connection and pass it to a new user
		 * thread.
		 */
		while (true) {
			try {
				userSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxUsersCount; i++) {
					if (threads[i] == null) {
						threads[i] = new userThread(userSocket, threads);
						threads[i].start();
						break;
					}
				}
				if (i == maxUsersCount) {
					PrintStream output_stream = new PrintStream(userSocket.getOutputStream());
					output_stream.println("#busy");
					output_stream.close();
					userSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

/*
 * Threads
 */
class userThread extends Thread {

	private String name = null;
	private BufferedReader input_stream = null;
	private PrintStream output_stream = null;
	private Socket userSocket = null;
	private final userThread[] threads;
	private int maxUsersCount;
	private List<String> friendlist = new ArrayList<String>();
	private List<String> requestlist =  new ArrayList<String>();
	// private boolean goodName = true;

	public userThread(Socket userSocket, userThread[] threads) {
		this.userSocket = userSocket;
		this.threads = threads;
		maxUsersCount = threads.length;
	}

	public void run() {
		int maxUsersCount = this.maxUsersCount;
		userThread[] threads = this.threads;

		try {
			/*
			 * Create input and output streams for this client.
			 * Read user name.
			 */
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
			output_stream = new PrintStream(userSocket.getOutputStream());
			output_stream.println("what is your name???");
			name = input_stream.readLine().trim();
			int c = 0;
			//System.out.println("111"+name);
			// output_stream.println("#Welcome <" + name + ">");
			synchronized (userThread.class) {
			for (int j = 0; j < maxUsersCount; j++){
				if(threads[j] != null && threads[j] != this){
					if(threads[j].name.equals(name)){
					output_stream.println("duplicated user name!!!");
					c++;
				}
			}
		}
	}
			if(c!=0){
					synchronized (userThread.class) {
						for (int i = 0; i < maxUsersCount; i++) {
							if (threads[i] == this) {
								threads[i] = null;
							}
						}
					}
					
					input_stream.close();
					output_stream.close();
					userSocket.close();
					}
				
			else{
				for( int j = 0; j < maxUsersCount; j++){
					if(threads[j] != null && threads[j] != this){

					threads[j].output_stream.println("#Newuser " + name);
				}
			}
		}
			
		
			// for (int i = 0; i < maxUsersCount; i++) {
					
			// 	if(threads[i] != null && threads[i] != this){
			// 			threads[i].output_stream.println("#newuser <" + name + "> enter chat room!!!");
			// 		}
					
			// 		}





			/* Welcome the new user. */
			output_stream.println("#Welcome " + name);

			/* Start the conversation. */
			while (true) {
				String words = input_stream.readLine();
				if (words.startsWith("#Bye")){
					break;
				}
				else if (words.startsWith("#friendme")){
					String rname = words.split(" ")[1];
					int count = 0;
					// boolean b = true;
					// for (int i = 0; i < maxUsersCount; i++){
					// if (threads[i] == null || rname.equals(this.name) || !threads[i].name.equals(rname)){
					// 	count++;
					// }
					// }
					// if(count == 5){
					// 	b = false;
					// }	
						synchronized (userThread.class){			
						for (int i = 0; i < maxUsersCount; i++){
							//System.out.println(threads[i].name+ " "+rname+" "+this.name);
							if (threads[i] != null && !rname.equals(this.name) && threads[i].name.equals(rname)){
							//System.out.println(threads[i].name +"   nima " + rname);
								threads[i].requestlist.add(name);
								threads[i].output_stream.println("#friendme " + name);
								count++;
								// break;	
							 }
							
							}
							if(count == 0){
								output_stream.println("wrong name !!!");

								}
						}
				}
				
				else if (words.startsWith("#friends")){
					String fname = words.split(" ")[1];
						if (this.requestlist.contains(fname)){
							this.friendlist.add(fname);
							this.requestlist.remove(fname);
							System.out.println("test2"+ this.friendlist );
							synchronized (userThread.class){
								for(int i = 0; i < maxUsersCount; i++){
									if(threads[i] != null && threads[i].name.equals(fname)){
										threads[i].friendlist.add(name);
										threads[i].output_stream.println("#OKfriends " + fname + " " + name);
										this.output_stream.println("#OKfriends " + fname + " " + name);
										System.out.println("test3"+ threads[i].friendlist );
									}
								}
							}
						}else{
							output_stream.println("no such request!!!");
						}
					}
				else if (words.startsWith("#FriendRequestDenied")){
					String dname = words.split(" ")[1];
					if(this.requestlist.contains(dname)){
						this.requestlist.remove(dname);
					//	this.output_stream.println("#FriendRequestDeny <"+ dname + ">");
						//System.out.println("test4"+ this.friendlist );
						synchronized(userThread.class){
								for(int i = 0; i < maxUsersCount; i++){
									if(threads[i] != null && threads[i].name.equals(dname)){
										threads[i].output_stream.println("#DenyFriendRequest " + name);
									}
								}
						}
					}else{
						output_stream.println("no such request!!!");
					}
				}
				else if (words.startsWith("#unfriend")){
					String dname = words.split(" ")[1];
					if(this.friendlist.contains(dname)){
						this.friendlist.remove(dname);
						synchronized(userThread.class){
							for(int i = 0; i < maxUsersCount; i++){
								if(threads[i] != null && threads[i].name.equals(dname)){
									threads[i].friendlist.remove(name);
									threads[i].output_stream.println("#NotFriends " + dname + " " + name);
									this.output_stream.println("#NotFriends " + dname + " " + name);
									System.out.println("test5 " + threads[i].friendlist);
								}
							}
						}

					}else{
						output_stream.println("no such friend!!!");
					}
				}

				else {
					for (int i = 0; i < maxUsersCount; i++){
					if(threads[i] != null && this.friendlist.contains(threads[i].name)){
							threads[i].output_stream.println("#newStatus "+name+": " + words);
					}
				}
			}
			
		}

			for (int i = 0; i < maxUsersCount; i++){
				if(threads[i] != null && threads[i] != this){
					threads[i].output_stream.println("#Leave " + name);
				}
			}
			output_stream.println("#Bye!");

			// conversation ended.

			/*
			 * Clean up. Set the current thread variable to null so that a new user
			 * could be accepted by the server.
			 */
			synchronized (userThread.class) {
				for (int i = 0; i < maxUsersCount; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			/*
			 * Close the output stream, close the input stream, close the socket.
			 */
			input_stream.close();
			output_stream.close();
			userSocket.close();
		} catch (IOException e) {
		}
	}
}




