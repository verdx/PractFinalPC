package parte2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Map;


import mensajes.Mensaje;
import mensajes.MensajeConfListaArchivos;
import mensajes.MensajeConfListaUsuarios;
import mensajes.MensajeEmisorPreparadoSC;
import mensajes.MensajeEmitirFichero;

public class ThreadOyServidor extends Thread {

	String username;
	
	Socket s;
	ObjectInputStream fin;
	
	Cliente cliente;
	
	boolean exit;
	
	public ThreadOyServidor(Socket s, String username, Cliente cliente) {
		exit = false;
		this.s = s;
		this.username = username;
		this.cliente = cliente;
		
		// Creamos el canal de escucha y el de salida para el servidor
		try {
			fin = new ObjectInputStream(s.getInputStream());
		} catch (IOException e) {
			System.out.println("Ha habido algún fallo al conseguir los streams de input y output del cliente " + username);
			e.printStackTrace();
			closeAll();
			return;
		}

	}

	public void run()  {
		
		while(!exit) {
			Mensaje m = null;
			try {
				m = (Mensaje) fin.readObject();
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Problema recibiendo el mensaje en el os del cliente " + username);
				e.printStackTrace();
			}
			
			switch(m.getTipo()) {
			case MENSAJE_CONFIRMACION_CONEXION:
				System.out.println("Se ha establecido la conexión con el servidor.");
				break;
			case MENSAJE_CONFIRMACION_LISTA_USUARIOS:
				printUsers(((MensajeConfListaUsuarios) m).getUsers());
				break;
			case MENSAJE_CONFIRMACION_LISTA_ARCHIVOS:
				printFiles(((MensajeConfListaArchivos) m).getArchivos());
				break;
			case MENSAJE_EMITIR_FICHERO:
				cliente.emitirArchivo(((MensajeEmitirFichero) m).getFilename(), ((MensajeEmitirFichero) m).getUser());
				break;
			case MENSAJE_PREPARADO_EMISORSC:
				cliente.recibirArchivo(((MensajeEmisorPreparadoSC) m).getHost(), ((MensajeEmisorPreparadoSC) m).getPort());
				break;
			case MENSAJE_CONFIRMACION_CERRAR_CONEXION:
				System.out.println("Saliendo del sistema");
				exit = true;
			default:
				break;	
			}
		}
		return;
	}
	
	private void printUsers(String[] users) {
		System.out.println("\nUsers:");
		for(String s: users) {
			System.out.println("-" + s);
		}
	}
	
	private void printFiles(Map<String, String[]> files) {
		for(String user: files.keySet()) {
			System.out.println("\n" + user + ": ");
			for(String file: files.get(user)) {
				System.out.println("  \u2514" + file);
			}
		}
	}
	

	private void closeAll() {
		try {
			fin.close();
			s.close();
		} catch (IOException e) {
			System.out.println("Fallo al cerrar los streams o el socket en el cliente " + username);
			e.printStackTrace();
		}
	}
}
