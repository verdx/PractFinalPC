package parte1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ThreadConexion extends Thread {
	
	Socket s;
	BaseDeDatos bd;
	File file;
	Scanner fileReader;
	Scanner streamReader;
	
	public ThreadConexion(Socket s, BaseDeDatos bd) {
		this.s = s;
		this.bd = bd;
	}

	
	public void run() {
		
		// Creamos los canales de comunicacion con el cliente
		OutputStream fout;
		InputStream fin;
		try {
			fout = s.getOutputStream();
			fin = s.getInputStream();
		} catch (IOException e) {
			System.out.println("Ha habido algún fallo al conseguir los streams de input y output.");
			e.printStackTrace();
			return;
		}
		
		//Recibimos del cliente el nombre del archivo que busca
		streamReader = new Scanner(fin);
		String fn = streamReader.hasNext() ? streamReader.next() : "";
		
		//Creamos un stream de lectura del archivo de la base de datos
		FileInputStream fin_file = null;
		try {
			fin_file = new FileInputStream(bd.readFile(fn));
		} catch (FileNotFoundException e) {
			System.out.println("No existe el archivo que se busca");
			e.printStackTrace();
			return;
		}
		
		byte[] bytes = new byte[16*1024];

        int count;
        try {
			while ((count = fin_file.read(bytes)) > 0) {
			    fout.write(bytes, 0, count);
			}
		} catch (IOException e) {
			System.out.println("Fallo al transmitir el archivo");
		}

        try {
			fout.close();
			fin.close();
	        fin_file.close();
	        s.close();
		} catch (IOException e) {
			System.out.println("Fallo al cerrar los streams o el socket");
			e.printStackTrace();
		}
        
	}
}
