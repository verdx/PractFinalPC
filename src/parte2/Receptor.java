package parte2;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;

import mensajes.MensajeSubirArchivos;

public class Receptor extends Thread {
	

	ObjectInputStream fin;
	ObjectOutputStream fout;
	Socket s;
	
	int port;
	String host;
	
	List<File> files;
	Lock files_lock;
	
	public Receptor(String host, int port, List<File> files, Lock files_lock, ObjectOutputStream fout) {
		this.host = host;
		this.port = port;
		this.files = files;
		this.files_lock = files_lock;
		this.fout = fout;
	}
	
	public void run() {
		
		// Conectamos el socket al emisor
		try {
			s = new Socket(host, port);
		} catch (IOException e) {
			System.out.println("Problema en el receptor al conectarse al emisor: " + e.getLocalizedMessage());
			return;
		}
		
		
		// Iniciamos el stream de entrada
		try {
			fin = new ObjectInputStream(s.getInputStream());
		} catch (IOException e) {
			System.out.println("Problema al crear el stream de entrada del receptor: " + e.getLocalizedMessage());
		}

		// Recibimos el archivo por el stream
		FileContents fileabs = null;
		try {
			fileabs = (FileContents) fin.readObject();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Problema en el receptor al recibir el archivo: " + e.getLocalizedMessage());
		}

		// Guardamos el archivo
		Path path = Paths.get(System.getProperty("user.dir") + "/" + fileabs.getFilename());
		try {
			Files.write(path, fileabs.getContents());
		} catch (IOException e) {
			System.out.println("Problema escribiendo el archivo: " + e.getLocalizedMessage());
		}
		addFiles(Arrays.asList(path.toFile()));
		return;
	}

	private void addFiles(List<File> filesin) {	
		List<String> in = new ArrayList<String>();

		files_lock.lock();
		for(File f: filesin) {
			files.add(f);
			in.add(f.getName());
		}
		files_lock.unlock();

		try {

			fout.writeObject(new MensajeSubirArchivos(in));
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al enviar el mensaje: " + e.getLocalizedMessage());
		}

	}

}
