import java.io.*;
import java.util.*;
import java.net.*;
import common.Question;

public class Server {
	public static void main(String[] args) {
		//Check arguments:
		if(args.length!=4) {
			System.out.println("Usage: Server <port> <folder> <reward> <ded>");
			System.exit(0);
		}
		//Check if folder exists:
		File fchecker=new File(args[1]);
		if(!fchecker.exists() || !fchecker.isDirectory()) {
			System.out.println("Error: Invalid folder " + args[1]);
			System.exit(1);
		}
		//Create ServerSocket object:
		ServerSocket conn=null;
		try {
			conn=new ServerSocket(Integer.parseInt(args[0]));
		} catch(IOException o) {
			System.out.println("Fatal I/O error!");
			System.exit(1);
		}
		//Status message:
		System.out.println("Listening on port " + args[0]);
		//Temporary Socket object:
		Socket s=null;
		//Check for connection and handle clients:
		while(true) {
			try {
				//Get socket:
				s=conn.accept();
				//Set socket timeout to 5 minutes (300 seconds):
				s.setSoTimeout(300000);
			} catch(SocketException so) {
				System.out.println("Socket error!");
				continue;
			} catch(IOException i) {
				System.out.println("I/O Error!");
				continue;
			}
			//Start client handler thread:
			new ClientHandler(s, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3])).start();
		}
	}
}
