package parte2;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import mensajes.Mensaje;
import mensajes.MensajeCerrarConexion;
import mensajes.MensajeConexion;
import mensajes.MensajeConfCerrarConexion;
import mensajes.MensajeConfConexion;
import mensajes.MensajeEmisorPreparadoCS;
import mensajes.MensajeEmisorPreparadoSC;
import mensajes.MensajeEmitirFichero;
import mensajes.MensajePedirFichero;

public class ThreadOyCliente extends Thread {
	
	Socket s;
	BaseDeDatos bd;
	File file;
	ObjectInputStream fin;
	ObjectOutputStream fout;
	
	public ThreadOyCliente(Socket s, BaseDeDatos bd) {
		this.s = s;
		this.bd = bd;
		
		// Creamos los canales de comunicacion con el cliente
		try {
			fin = new ObjectInputStream(s.getInputStream());
			fout = new ObjectOutputStream(s.getOutputStream());
		} catch (IOException e) {
			System.out.println("Ha habido algún fallo al conseguir los streams de input y output.");
			e.printStackTrace();
			closeAll();
			return;
		}
	}

	
	public void run() {
		while(true) {
			Mensaje m = null;
			try {
				m = (Mensaje) fin.readObject();
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Problema recibiendo el mensaje en un oc");
				e.printStackTrace();
				closeAll();
			}
			
			switch(m.getTipo()) {
			case MENSAJE_CONEXION:
				addUser(((MensajeConexion) m).getUsername(), ((MensajeConexion) m).getFiles());
				break;
			case MENSAJE_CERRAR_CONEXION:
				removeUser(((MensajeCerrarConexion) m).getUsername());
				break;
			case MENSAJE_PEDIR_FICHERO:
				pedirFichero(((MensajePedirFichero) m).getFilename(), ((MensajePedirFichero) m).getUser());
			case MENSAJE_PREPARADO_EMISORCS:
				emisorPreparado(((MensajeEmisorPreparadoCS) m).getUser(), ((MensajeEmisorPreparadoCS) m).getHost(), 
						((MensajeEmisorPreparadoCS) m).getPort());
			default:
				break;
				
				
			}
		}
        
	}
	
	private void addUser(String username, String[] files) {
		bd.addUser(username, files , fin, fout);
		try {
			fout.writeObject(new MensajeConfConexion());
			fout.flush();
		} catch (IOException e) {
			System.out.println("Fallo al enviar mensaje de confirmacion de conexion");
			e.printStackTrace();
		}
	}
	
	private void removeUser(String username) {
		bd.removeUser(username);
		try {
			fout.writeObject(new MensajeConfCerrarConexion());
			fout.flush();
		} catch (IOException e) {
			System.out.println("Fallo al enviar mensaje de confirmacion de conexion");
			e.printStackTrace();
		}
	}
	
	private void pedirFichero(String filename, String user) {
		ObjectOutputStream auxout = bd.getUserFout(bd.getOwner(filename));
		try {
			auxout.writeObject(new MensajeEmitirFichero(filename, user));
			auxout.flush();
		} catch (IOException e) {
			System.out.println("Fallo al enviar mensaje de confirmacion de conexion");
			e.printStackTrace();
		}
	}
	
	private void emisorPreparado(String user, String host, int port) {
		ObjectOutputStream auxout= bd.getUserFout(user);
		try {
			auxout.writeObject(new MensajeEmisorPreparadoSC(host, port));
			auxout.flush();
		} catch (IOException e) {
			System.out.println("Problema al mandas MensajeEmisorPreparado del servidor al cliente receptor");
			e.printStackTrace();
		}
		
	}
	
	private void closeAll() {
		try {
			fin.close();
	        s.close();
		} catch (IOException e) {
			System.out.println("Fallo al cerrar los streams o el socket");
			e.printStackTrace();
		}   
	}
}
