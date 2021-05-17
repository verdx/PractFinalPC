package parte2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.concurrent.Semaphore;

import mensajes.Mensaje;
import mensajes.MensajeConfListaArchivos;
import mensajes.MensajeConfListaUsuarios;
import mensajes.MensajeEmisorPreparadoSC;
import mensajes.MensajeEmitirFichero;

public class OyenteCliente extends Thread {

	private ObjectInputStream fin;
	
	private Cliente cliente;
	
	private boolean exit;
	
	private Semaphore input_sem;
	
	public OyenteCliente(ObjectInputStream fin, String username, Cliente cliente, Semaphore input_sem) {
		exit = false;
		this.cliente = cliente;
		this.fin = fin;
		this.input_sem = input_sem;
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
				System.out.println("No se ha recibido correctamente el mensaje");
			} else  {
				switch(m.getTipo()) {
				case MENSAJE_CONFIRMACION_LISTA_USUARIOS:
					printUsers(((MensajeConfListaUsuarios) m).getUsers());
					input_sem.release();
					break;
				case MENSAJE_CONFIRMACION_LISTA_ARCHIVOS:
					printFiles(((MensajeConfListaArchivos) m).getArchivos());
					input_sem.release();
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
					break;
				case MENSAJE_ARCHIVO_NO_EXISTE:
					System.out.println("El archivo no existe");
					input_sem.release();
					break;
				default:
					System.out.println("Ha llegado un mensaje desconocido de tipo: " + m.getTipo());
					break;	
				} 
			}
		}
		return;
	}
	
	private void printUsers(String[] users) {
		System.out.println("Users:");
		for(String s: users) {
			System.out.println("-" + s);
		}
	}
	
	private void printFiles(Map<String, String[]> files) {
		for(String user: files.keySet()) {
			System.out.println("-" + user + ": ");
			for(String file: files.get(user)) {
				System.out.println("  \u2514" + file + "\n");
			}
		}
	}
}
