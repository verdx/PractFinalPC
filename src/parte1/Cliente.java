package parte1;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Cliente extends Thread {
	
	String fn;
	FileOutputStream fout_file;
	Socket s;
	String host;
	
	public Cliente(String fn, String host) {
		this.fn = fn;
		this.host = host;
		try {
			s = new Socket(host, 500);
		} catch (IOException e) {
			System.out.println("No se ha podido crear el Socket");
			e.printStackTrace();
		}
	}

	public Cliente(String fn) {
		this.fn = fn;
		this.host = "127.0.0.1";
		try {
			s = new Socket(host, 500);
		} catch (IOException e) {
			System.out.println("No se ha podido crear el Socket");
			e.printStackTrace();
		}
	}
	
	public void run() {
		
	}
}
