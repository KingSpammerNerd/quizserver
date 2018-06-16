package common;
import java.io.*;
import nu.xom.*;

public class Question implements Serializable{
	//Question content, options, answer:
	private String content, answer;
	private String[] options;
	
	//Server-side Constructor:
	public Question(File qinput) throws FileNotFoundException {
		//Check if file exists:
		if(!qinput.exists()) {
			throw new FileNotFoundException();
		}
		//Create Builder:
		Builder builder=new Builder();
		//Create Document:
		Document doc=null;
		try {
			doc=builder.build(qinput);
		} catch(IOException i) {
			System.out.println("I/O Error reading file " + qinput + "!");
			//return;
		} catch(ParsingException p) {
			System.out.println("Error parsing file " + qinput + "!");
			//return;
		}
		//Get root element:
		Element doc_root=doc.getRootElement();
		//Get content element and read its value:
		this.content=doc_root.getFirstChildElement("content").getValue();
		//Get options:
		Element option=doc_root.getFirstChildElement("options");
		//Store option text:
		this.options=new String[4];
		options[0]=new String(option.getFirstChildElement("optiona").getValue());
		options[1]=new String(option.getFirstChildElement("optionb").getValue());
		options[2]=new String(option.getFirstChildElement("optionc").getValue());
		options[3]=new String(option.getFirstChildElement("optiond").getValue());
		//Store answer:
		this.answer=doc_root.getFirstChildElement("answer").getValue();
	}
	
	//Client-side Constructor:
	public Question(String content, String[] options, String answer) {
		this.content=content;
		this.options=options;
		this.answer=answer;
	}
	
	//Accessors:
	public String getContent() {
		return this.content;
	}
	public String[] getOptions() {
		return this.options;
	}
	public String getAnswer() {
		return this.answer;
	}
}
