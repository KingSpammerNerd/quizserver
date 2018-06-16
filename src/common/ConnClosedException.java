package common;
public class ConnClosedException extends Exception {
	private String addr;
	public ConnClosedException(String a) {
		addr=new String(a);
	}
	public String toString() {
		return("Connection to " + addr + " closed.");
	}
}
