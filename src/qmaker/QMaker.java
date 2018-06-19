import nu.xom.*;
import java.io.*;
import java.util.*;

public class QMaker {
	private static int getChoice(Scanner s) {
		System.out.println("Which option is the answer [1-4]? ");
		int ch=s.nextInt();
		if(ch>4 || ch<1) {
			System.out.println("Enter a valid option!");
			getChoice(s);
		}
		//Reset scanner:
		s.close();
		s=new Scanner(System.in);
		return ch;
	}
	public static void main(String[] args) {
		//Check arguments:
		if(args.length!=1) {
			System.out.println("Usage: QMaker <output file>");
			System.exit(0);
		}
		//Open keyboard input:
		Scanner kb_in=new Scanner(System.in);
		//Check if file exists:
		File outfile=new File(args[0]);
		if(outfile.exists()) {
			System.out.print("File " + args[0] + " already exists. Overwrite [y/N]? ");
			if(kb_in.nextLine().charAt(0)!='y') {
				System.out.println("Exiting...");
				System.exit(0);
			}
		}
		//Open file output stream:
		PrintWriter fout=null;
		try {
			fout=new PrintWriter(outfile);
		} catch(IOException e) {
			System.out.println("Error opening file for writing!");
			System.exit(1);
		}
		//Root element:
		Element root=new Element("question");
		//XML document:
		Document doc=new Document(root);
		//Get question data from user:
		String qcontent=null;
		String[] opts=new String[4];
		String answer=null;
		System.out.print("Enter question text: ");
		qcontent=kb_in.nextLine();
		System.out.print("Enter option A: ");
		opts[0]=kb_in.nextLine();
		System.out.print("Enter option B: ");
		opts[1]=kb_in.nextLine();
		System.out.print("Enter option C: ");
		opts[2]=kb_in.nextLine();
		System.out.print("Enter option D: ");
		opts[3]=kb_in.nextLine();
		answer=new String(opts[getChoice(kb_in)]);
		//Add elements to root element:
		Element content=new Element("content");
		content.appendChild(qcontent);
		root.appendChild(content);
		Element options=new Element("options");
		Element optiona=new Element("optiona");
		optiona.appendChild(opts[0]);
		options.appendChild(optiona);
		Element optionb=new Element("optionb");
		optionb.appendChild(opts[1]);
		options.appendChild(optionb);
		Element optionc=new Element("optionc");
		optionc.appendChild(opts[2]);
		options.appendChild(optionc);
		Element optiond=new Element("optiond");
		optiond.appendChild(opts[3]);
		options.appendChild(optiond);
		root.appendChild(options);
		Element ans=new Element("answer");
		ans.appendChild(answer);
		root.appendChild(ans);
		//Write document to file:
		try {
			fout.write(doc.toXML());
		} catch(Exception i) {
			System.out.println("Exception caught: " + i);
		} finally {
			fout.close();
			kb_in.close();
		}
	}
}
