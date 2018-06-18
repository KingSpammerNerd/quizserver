import java.util.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.awt.event.*;
import java.awt.*;
import common.*;

public class Client {
	//Socket object:
	private Socket sock;
	//Application frame object:
	private JFrame fmain;
	//Starting dialog box:
	private JFrame startd;
	
	//IP address field:
	private JTextField ipbox;
	//Port number field:
	private JTextField portbox;
	
	//DataInputStream, to read server data:
	DataInputStream sv_in;
	//DataOutputStream, to send data to server:
	DataOutputStream sv_out;
	//ObjectInputStream, to receive question objects:
	ObjectInputStream question_in;
	
	//Candidate's score:
	private int score=0;
	
	//Constructor, creates main quiz frame and calls quizStart():
	public Client() {
		//Open quiz start Dialog:
		this.quizStart();
		
	}
	
	//Create an error message Dialog box:
	private void errBox(String errortext, JFrame parent) {
		//Create error message:
		JDialog er=new JDialog(parent);
		er.setLayout(new GridLayout(2, 1));
		er.setSize(120, 85);
		//Add label:
		er.add(new JLabel(errortext));
		//Create button:
		JButton cl=new JButton("OK");
		cl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				//Dispose error dialog:
				er.dispose();
			}
		});
		//Add button:
		er.add(cl);
		//Make error message visible:
		er.setVisible(true);
	}
	
	//Shows the first Dialog box, allowing the user to enter the IP address and port number of the quiz server. If the connection is successful, the method quizInit() is called.
	private void quizStart() {
		//Dialog box for IP address and port:
		startd=new JFrame();
		startd.setLayout(new GridLayout(3, 2));
		startd.setSize(300, 100);
		startd.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		//Initialize TextFields for IP address and port number:
		ipbox=new JTextField(20);
		portbox=new JTextField(20);
		//Button to start connection:
		JButton cbutton=new JButton("Connect");
		cbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					sock=new Socket(ipbox.getText(), Integer.valueOf(portbox.getText()));
					throw new Exception() {
						public String toString() {
							return new String("success");
						}
					};
				} catch(UnknownHostException u) {
					errBox("Unknown host: " + ipbox.getText() + ':' + portbox.getText(), startd);
				} catch(IOException i) {
					errBox("I/O Error!", startd);
				} catch(Exception exc) {
					if(exc.toString().equals("success")) quizInit();
				}
			}
		});
		//Add elements to startd:
		startd.add(new JLabel(" IP address "));
		startd.add(ipbox);
		startd.add(new JLabel(" Port "));
		startd.add(portbox);
		startd.add(new JLabel(""));
		startd.add(cbutton);
		//Make startd visible:
		startd.setVisible(true);
	}
	
	//Initialize all data streams and prepare to start quiz:
	private void quizInit() {
		try {
			//Initialize data streams:
			sv_in=new DataInputStream(sock.getInputStream());
			sv_out=new DataOutputStream(sock.getOutputStream());
			question_in=new ObjectInputStream(sock.getInputStream());
			//Receive READY signal from server:
			if(sv_in.readUTF().equals("READY")) {
				//Send back "READY" to server:
				sv_out.writeUTF("READY");
				//Get number of questions:
				int no_of_questions=Integer.valueOf(sv_in.readUTF());
				if(sv_in.readUTF().equals("START")) {
					//Start test:
					sv_out.writeUTF("OK");
					//Close start dialog:
					startd.dispose();
					//Flush sv_out:
					sv_out.flush();
					//Start quiz:
					quizMain(no_of_questions);
				}
			}
		} catch(IOException ioe) {
			errBox("I/O Error!", fmain);
			System.exit(1);
		}
	}
	
	//These objects were taken out of quizMain() to avoid compile errors:
	//Question object:
	private Question qtemp=null;
	//Current question number:
	private int curq=0;
	//Option selector:
	JComboBox<String> opts=new JComboBox<String>();
	
	//Start quiz client, takes number of questions as argument:
	private void quizMain(int n) {
		//Main application frame:
		fmain=new JFrame();
		fmain.setSize(400, 450);
		fmain.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		//Add 5 rows: selector buttons, question content, previous answer (if any), option selector, "Select answer" and submit buttons:
		fmain.setLayout(new GridLayout(8, 1));
		//Create Question content field:
		JTextArea qcontent=new JTextArea(400, 400);
		qcontent.setEditable(false);
		//Previous answer field:
		JTextField prevans=new JTextField(400);
		prevans.setEditable(false);
		
		//Array to store answers:
		String[] answers=new String[n];
		//Array to store responses:
		String[] responses=new String[n];
		
		//Button panel:
		JPanel bpanel=new JPanel();
		bpanel.setLayout(new GridLayout(1, 2));
		//"Save answer" button:
		JButton saveans=new JButton("Save answer");
		saveans.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					//Tell the server we're sending a response:
					sv_out.writeUTF("A");
					if(sv_in.readUTF().equals("OK")) {
						//Send question number:
						sv_out.writeUTF(String.valueOf(curq));
						//Wait for response and send submitted answer:
						if(sv_in.readUTF().equals("ANS")) {
							sv_out.writeUTF((String)opts.getSelectedItem());
						}
					}
					//Disable self:
					((JButton)e.getSource()).setEnabled(false);
					//Flush sv_out:
					sv_out.flush();
				} catch(IOException c) {
					errBox("I/O Error!", fmain);
				}
			}
		});
		//Submit button:
		JButton submitbutton=new JButton("Submit");
		submitbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					//Send "Submit" signal to server:
					sv_out.writeUTF("S");
					//Get response from server:
					if(sv_in.readUTF().equals("RESPS")) {
						//Send OK to server:
						sv_out.writeUTF("OK");
						//Flush sv_out:
						sv_out.flush();
						//Get user responses:
						for(int i=0; i<n; ++i) responses[i]=new String(sv_in.readUTF());
					}
					//Get answers from server:
					if(sv_in.readUTF().equals("SOLS")) {
						//Send OK to server:
						sv_out.writeUTF("OK");
						//Flush sv_out:
						sv_out.flush();
						//Get answers:
						for(int i=0; i<n; ++i) answers[i]=new String(sv_in.readUTF());
					}
					//Get score from server:
					score=Integer.valueOf(sv_in.readUTF());
					
					//Start shutdown:
					if(sv_in.readUTF().equals("BYE")) sv_out.writeUTF("BYE");
					//Close connections:
					sv_in.close();
					sv_out.close();
					question_in.close();
					
					//Kill fmain:
					fmain.dispose();
					//Build result string:
					StringBuilder fin=new StringBuilder();
					fin.append("RESULTS:\nRESPONSE\tANSWER\n");
					for(int i=0; i<n; ++i) fin.append(responses[i] + '\t' + answers[i] + '\n');
					fin.append("Score: " + score);
					//Display results:
					errBox(fin.toString(), null);
				} catch(IOException c) {
					errBox("I/O Error!", fmain);
				}
			}
		});
		//Add buttons to bpanel:
		bpanel.add(saveans);
		bpanel.add(submitbutton);
		
		//Make fmain visible:
		fmain.setVisible(true);
		//Create question button panel and add buttons to it:
		JButton[] qbuttons=new JButton[n];
		JPanel qbs=new JPanel();
		qbs.setLayout(new GridLayout(1, n));
		for(int i=0; i<n; ++i) {
			//Create button:
			qbuttons[i]=new JButton(String.valueOf(i+1));
			//Action for button:
			qbuttons[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						//Set previously selected button as enabled and disable selected button:
						qbuttons[curq].setEnabled(true);
						((JButton)e.getSource()).setEnabled(false);
						//Set question number:
						curq=Integer.valueOf(e.getActionCommand());
						//Send question request:
						sv_out.writeUTF("Q");
						//Flush sv_out:
						sv_out.flush();
						//Get question:
						if(sv_in.readUTF().equals("OK")) {
								//Send question number:
								sv_out.writeUTF(String.valueOf(curq));
								//Get question object:
								qtemp=(Question)question_in.readObject();
								//Get previous answer:
								prevans.setText(sv_in.readUTF());
								//Display questions content:
								qcontent.setText(qtemp.getContent());
								//Populate options list:
								for(String op: qtemp.getOptions()) opts.addItem(op);
						}
						//Enable saveans button:
						saveans.setEnabled(true);
					} catch(IOException ex) {
						errBox("I/O Error!", fmain);
					} catch(ClassNotFoundException cl) {
						//This should NEVER happen!
						errBox("Fatal Exception", fmain);
					}
				}
			});
			//Add button to panel:
			qbs.add(qbuttons[i]);
		}
		//Add question button panel:
		fmain.add(qbs);
		//Add question field:
		fmain.add(new JLabel("Question"));
		fmain.add(qcontent);
		//Add previous answer field:
		fmain.add(new JLabel("Previous answer"));
		fmain.add(prevans);
		//Add options dropdown:
		fmain.add(new JLabel("Options"));
		fmain.add(opts);
		//Add submit buttons:
		fmain.add(bpanel);
	}
	
	//Main method:
	public static void main(String[] args) {
		new Client();
	}
}
