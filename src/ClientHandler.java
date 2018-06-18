import java.util.*;
import java.io.*;
import java.net.*;
import common.*;

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
	//ObjectOutputStream, to send question objects:
	private ObjectOutputStream question_out;
	//Exception object:
	ConnClosedException cc;
	
	//Array of questions:
	private Question[] questions;
	//Array storing answers:
	private String[] answers;
	//Array storing responses:
	private String[] responses;
	
	//Score reward:
	int reward;
	//Score deduction:
	int ded;
	
	//Constructor, takes a socket object, folder name, reward and deduction:
	public ClientHandler(Socket sock, String folder, int reward, int ded) {
		//Store socket object:
		this.client_sock=sock;
		//Store score reward:
		this.reward=reward;
		//Store score deduction:
		this.ded=ded;
		
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
			//Temp string:
			String req=null;
			client_out.writeUTF("START");
			if(client_in.readUTF().equals("OK")) while(true) {
				//Get request code:
				req=new String(client_in.readUTF());
				//Check and handle request:
				if(req.equals("Q")) {
					handleQ();
					continue;
				}
				if(req.equals("A")) {
					handleA();
					continue;
				}
				if(req.equals("S")) {
					handleS();
					break;
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
	
	//Method to flush all data streams:
	private void flushAll() throws IOException {
		client_out.flush();
		question_out.flush();
	}
	
	//Handle Q request:
	private void handleQ() throws IOException {
		printMsgln("Sending question to " + client_ip);
		client_out.writeUTF("OK");
		//Get array index:
		int ind=(Integer.valueOf(client_in.readUTF()))-1;
		//Send question object from array:
		question_out.writeObject(questions[ind]);
		//Send previously submitted answer, if any:
		if(responses[ind]==null)
			client_out.writeUTF("None");
		else
			client_out.writeUTF(responses[ind]);
		flushAll();
	}
	
	//Handle A request:
	private void handleA() throws IOException {
		printMsgln("Getting answer from " + client_ip);
		client_out.writeUTF("OK");
		//Get array index:
		int ind=(Integer.valueOf(client_in.readUTF()))-1;
		//Send ready message:
		client_out.writeUTF("ANS");
		//Get answer:
		responses[ind]=client_in.readUTF();
		flushAll();
	}
	
	//Handle S request:
	private void handleS() throws IOException {
		printMsgln(client_ip + " has submitted their test");
		//Score variable;
		int score=0;
		//Send candidate's responses:
		client_out.writeUTF("RESPS");
		if(client_in.readUTF().equals("OK")) {
			for(String s: responses) {
				client_out.writeUTF(s);
				client_out.flush();
			}
		}
		//Send answers and score:
		client_out.writeUTF("SOLS");
		if(client_in.readUTF().equals("OK")) {
			for(int i=0; i<questions.length; ++i) {
				//Change score:
				if(questions[i].getAnswer().equals(responses[i]))
					score+=reward;
				else if(!questions[i].getAnswer().equals(responses[i]) && !responses[i].equals(""))
					score-=ded;
				else
					score+=0;
				//Send answer:
				client_out.writeUTF(questions[i].getAnswer());
				client_out.flush();
			}
			//Send score:
			client_out.writeUTF(String.valueOf(score));
		}
		flushAll();
	}
}
