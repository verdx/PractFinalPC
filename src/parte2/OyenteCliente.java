package parte2;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import mensajes.Mensaje;
import mensajes.MensajeArchivosSubidos;
import mensajes.MensajeConfListaArchivos;
import mensajes.MensajeConfListaUsuarios;
import mensajes.MensajeEmisorPreparadoCS;
import mensajes.MensajeEmisorPreparadoSC;
import mensajes.MensajeEmitirFichero;

public class OyenteCliente extends Thread {
	
	private List<File> files;
	private Lock files_lock;

	private ObjectInputStream fin;
	private ObjectOutputStream fout;
	
	private boolean exit;
	
	private int portout;
	private String myhost;
	
	String prompt;
	
	public OyenteCliente(ObjectInputStream fin, ObjectOutputStream fout, 
			String username,int portout, String myhost,
			List<File> files, Lock files_lock) {
		exit = false;
		this.fin = fin;
		this.fout = fout;
		this.portout = portout;
		this.myhost = myhost;
		this.files = files;
		this.files_lock = files_lock;
		prompt = "\n" + username + ">";
	}

	public void run()  {
		
		while(!exit) {
			Mensaje m = null;
			try {
				m = (Mensaje) fin.readObject();
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Problema al recibir el mensaje :" + e.getLocalizedMessage());
			}
			
			if(m == null) {
				System.out.print(  "No se ha recibido correctamente el mensaje" + prompt);
			} else  {
				switch(m.getTipo()) {
				case MENSAJE_CONFIRMACION_LISTA_USUARIOS:
					printUsers(((MensajeConfListaUsuarios) m).getUsers());
					break;
				case MENSAJE_CONFIRMACION_LISTA_ARCHIVOS:
					printFiles(((MensajeConfListaArchivos) m).getArchivos());
					break;
				case MENSAJE_EMITIR_FICHERO:
					emitirArchivo(((MensajeEmitirFichero) m).getFilename(), ((MensajeEmitirFichero) m).getUser());
					break;
				case MENSAJE_PREPARADO_EMISORSC:
					recibirArchivo(((MensajeEmisorPreparadoSC) m).getHost(), ((MensajeEmisorPreparadoSC) m).getPort());
					break;
				case MENSAJE_CONFIRMACION_CERRAR_CONEXION:
					System.out.println(  "Saliendo del sistema");
					exit = true;
					break;
				case MENSAJE_ARCHIVO_NO_EXISTE:
					System.out.print( "El archivo no existe" + prompt);
					break;
				case MENSAJE_ARCHIVOS_SUBIDOS:
					System.out.print("Se ha[n] subido correctamente " + ((MensajeArchivosSubidos) m).getCorrectos() + " archivo[s] al servidor" + prompt);
					break;
				default:
					System.out.print("Ha llegado un mensaje desconocido de tipo: " + m.getTipo() + prompt);
					break;	
				} 
			}
		}
		return;
	}
	
	private void emitirArchivo(String filename, String user_receptor) {
		File file = getFile(filename);
		if(file == null) {
			System.out.print("El fichero no parece existir" + prompt);
			return;
		}

		boolean puerto_correcto = false;
		Emisor emisor = null;

		while(!puerto_correcto) {
			puerto_correcto = true;
			try {
				emisor = new Emisor(portout, file);
				emisor.start();
			} catch(Exception e) {
				puerto_correcto = false;
			}
			portout += 1;
		}

		// Mandamos un mensaje para decir que estamos preparados y dar nuestra ip y puerto
		try {
			fout.writeObject(new MensajeEmisorPreparadoCS(myhost, portout - 1, user_receptor));
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al enviar el mensaje: " + e.getLocalizedMessage());
			return;
		}
		try {
			emisor.join();
		} catch (InterruptedException e) {
			System.out.println("Problema al cerrar el emisor: " + e.getLocalizedMessage());
		}
	}
	
	private void recibirArchivo(String myhost, int port) {
		Receptor receptor = new Receptor(myhost, port, files, files_lock, fout);
		receptor.start();
		try {
			receptor.join();
		} catch (InterruptedException e) {
			System.out.println( "Problema al cerrar el emisor: " + e.getLocalizedMessage());
		}
		System.out.println("Archivo recibido");
	}
	
	private void printUsers(String[] users) {
		System.out.println("Users:");
		for(String s: users) {
			System.out.println("-" + s);
		}
		System.out.print(prompt);
	}
	
	private void printFiles(Map<String, List<String>> map) {
		for(String user: map.keySet()) {
			System.out.println("-" + user + ": ");
			for(String file: map.get(user)) {
				System.out.println("  \u2514" + file);
			}
		}
		System.out.print(prompt);
	}
	
	private File getFile(String filename) {
		files_lock.lock();
		for(File f: files) {
			if(f.getName().equals(filename)) {
				return f;
			}
		}
		files_lock.unlock();
		System.out.println("No se ha encontrado el archivo.");
		return null;
	}
}
