//Finished.
package clienthandler;
import java.util.*;
import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
	//Socket to handle communication:
	private Socket client_sock;
	//Name of question folder:
	private String q_folder;
	//IP address of client:
	private String client_ip;
	//DataInputStream, to receive data:
	private DataInputStream client_in;
	//DataOutputStream, to send data:
	private DataOutputStream client_out;
	//ObjectOutputStream, to send questions:
	private ObjectOutputStream question_out;
	//Should the server keep running?
	private boolean sv_active;
	//Exception object:
	ConnClosedException cc;
	
	//Array of questions:
	private Question[] questions;
	//Array storing answers:
	private String[] answers;
	//Array storing responses:
	private String[] responses;
	
	//Constructor:
	public ClientHandler(Socket sock, String folder) {
		//Store socket object:
		this.client_sock=sock;
		
		//Store IP address:
		this.client_ip=sock.getRemoteSocketAddress().toString();
		try {
			//Initialize DataInputStream:
			this.client_in=new DataInputStream(client_sock.getInputStream());
			//Initialize DataOutputStream:
			this.client_out=new DataOutputStream(client_sock.getOutputStream());
			//Open ObjectOutputStream:
			question_out=new ObjectOutputStream(this.client_out);
		} catch(IOException i) {
			printMsgln("Error initializing connection with " + client_ip);
			return;
		}
		
		//Open question folder and store question objects:
		File qtemp=new File(folder);
		File[] f=qtemp.listFiles();
		this.questions=new Question[f.length];
		//Store questions:
		for(int i=0; i<f.length; ++i) {
			try {
				this.questions[i]=new Question(f[i]);
			} catch(FileNotFoundException ff) {}
		}
		
		//Store answers:
		this.answers=new String[questions.length];
		this.responses=new String[questions.length];
		for(int i=0; i<answers.length; ++i) answers[i]=questions[i].getAnswer();
		
		//Set active status to true:
		this.sv_active=true;
		
		//Initialize exception object:
		this.cc=new ConnClosedException(client_ip);
	}
	
	//Synchronized print methods:
	synchronized private void printMsgln(Object m) {
		System.out.println(m);
	}
	synchronized private void printMsg(Object m) {
		System.out.print(m);
	}
	
	//Main method:
	public void run() {
		try{
			//Send READY message to client, close connection in case of error:
			client_out.writeUTF("READY");
			//Wait for READY response from client, then start listening for responses:
			if(client_in.readUTF().equals("READY")) printMsgln("Incoming connection from " + client_ip);
			//Send number of questions:
			client_out.writeUTF(String.valueOf(questions.length));
			
			//Start test:
			client_out.writeUTF("START");
			if(client_in.readUTF().equals("OK")) while(sv_active) handleTest();
			
			//Send results after test:
			client_out.writeUTF("RESPS");
			if(client_in.readUTF().equals("OK")) {
				for(String s: responses) {
					client_out.writeUTF(s);
				}
			}
			client_out.writeUTF("SOLS");
			if(client_in.readUTF().equals("OK")) {
				for(Question q: questions) {
					client_out.writeUTF(q.getAnswer());
				}
			}
			
			//Close connection:
			client_out.writeUTF("BYE");
			if(client_in.readUTF().equals("BYE")) throw cc;		
		} catch(IOException i) {
			//Print error message:
			printMsgln("I/O Error with client " + client_ip);
		}
		catch(ConnClosedException c) {
			//Print message:
			printMsgln(c);
		} finally {
			//Close connections:
			try {
				client_out.close();
				client_in.close();
			} catch(IOException i) {
				printMsgln("Error closing connection to " + client_ip + '!');
			}
		}
	}
	
	//Question serving method:
	private void handleTest() throws IOException {
		if(client_in.readUTF().equals("Q")) {
			client_out.writeUTF("OK");
			//Get array index:
			int ind=(Integer.valueOf(client_in.readUTF()))-1;
			//Send question object from array:
			question_out.writeObject(questions[ind]);
		}
		else if(client_in.readUTF().equals("A")) {
			client_out.writeUTF("OK");
			//Get array index:
			int ind=(Integer.valueOf(client_in.readUTF()))-1;
			//Send ready message:
			client_out.writeUTF("ANS");
			//Get answer:
			responses[ind]=client_in.readUTF();
		}
		else if(client_in.readUTF().equals("S")) {
			//Kill question handler:
			sv_active=false;
		}
	}
}
