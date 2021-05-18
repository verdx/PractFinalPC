package parte2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import mensajes.Mensaje;
import mensajes.MensajeArchivoNoExiste;
import mensajes.MensajeArchivosSubidos;
import mensajes.MensajeCerrarConexion;
import mensajes.MensajeConexion;
import mensajes.MensajeConfCerrarConexion;
import mensajes.MensajeConfConexion;
import mensajes.MensajeConfListaArchivos;
import mensajes.MensajeConfListaUsuarios;
import mensajes.MensajeEmisorPreparadoCS;
import mensajes.MensajeEmisorPreparadoSC;
import mensajes.MensajeEmitirFichero;
import mensajes.MensajePedirFichero;
import mensajes.MensajeUsuarioCogido;
import mensajes.MensajeSubirArchivos;

public class OyenteServidor extends Thread {
	
	private Socket s;
	private BaseDeDatos bd;
	private ObjectInputStream fin;
	private ObjectOutputStream fout;
	private String username;
	
	public OyenteServidor(Socket s, BaseDeDatos bd) {
		this.s = s;
		this.bd = bd;
		
		// Creamos los canales de comunicacion con el cliente
		try {
			fin = new ObjectInputStream(s.getInputStream());
			fout = new ObjectOutputStream(s.getOutputStream());
		} catch (IOException e) {
			System.out.println("Problema al conseguir los streams desde el oyente: " + e.getLocalizedMessage());
			closeAll();
			return;
		}
	}

	
	public void run() {
		System.out.println("Comenzado thread oyente del servidor");
		
		boolean exit = false; //El bucle se acaba cuando el usuario se sale
		
		// Comenzamos el bucle de escucha de mensajes
		while(!exit) {
			Mensaje m = null;
			try {
				m = (Mensaje) fin.readObject();
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Problema recibiendo el mensaje en un oc: " + e.getLocalizedMessage());
				closeAll();
				return;
			}
			
			if(m == null) {
				System.out.println("No se ha recibido correctamente el mensaje");
			} else  {
				switch(m.getTipo()) {
				case MENSAJE_CONEXION:
					addUser(((MensajeConexion) m).getUsername(), ((MensajeConexion) m).getFiles());
					break;
				case MENSAJE_CERRAR_CONEXION:
					removeUser(((MensajeCerrarConexion) m).getUsername());
					exit = true;
					break;
				case MENSAJE_PEDIR_FICHERO:
					pedirArchivo(((MensajePedirFichero) m).getFilename(), ((MensajePedirFichero) m).getUser());
					break;
				case MENSAJE_PREPARADO_EMISORCS:
					emisorPreparado(((MensajeEmisorPreparadoCS) m).getUser(), ((MensajeEmisorPreparadoCS) m).getHost(), 
							((MensajeEmisorPreparadoCS) m).getPort());
					break;
				case MENSAJE_LISTA_USUARIOS:
					pedirUsuarios();
					break;
				case MENSAJE_LISTA_ARCHIVOS:
					pedirArchivos();
					break;
				case MENSAJE_SUBIR_ARCHIVOS:
					addFiles(username, ((MensajeSubirArchivos) m).getFileNames());
					break;
				default:
					break;
				}
			}
		}
        
	}
	
	private void addFiles(String username2, List<String> filenames) {
		int annadidos = bd.addFiles(username, filenames);
		try {
			fout.writeObject(new MensajeArchivosSubidos(annadidos));
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al enviar mensaje de confirmación de conexión: " + e.getLocalizedMessage());

		}
	}


	private void pedirUsuarios() {
		String[] users = bd.getUsers();
		try {
			fout.writeObject(new MensajeConfListaUsuarios(users));
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al enviar mensaje con la lista de usuarios: " + e.getLocalizedMessage());
		}
	}
	
	private void pedirArchivos() {
		Map<String, List<String>> files = bd.getFiles();
		try {
			fout.writeObject(new MensajeConfListaArchivos(files));
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al enviar mensaje con la lista de archivos: " + e.getLocalizedMessage());
		}
	}


	private void addUser(String username, List<String> files) {
		this.username = username;
		boolean annadido = bd.addUser(username, files , fin, fout);
		if(annadido)
			try {
				fout.writeObject(new MensajeConfConexion());
				fout.flush();
			} catch (IOException e) {
				System.out.println("Problema al enviar mensaje de confirmación de conexión: " + e.getLocalizedMessage());

			}
		else {
			try {
				fout.writeObject(new MensajeUsuarioCogido());
				fout.flush();
			} catch (IOException e) {
				System.out.println("Problema al enviar mensaje de usuario cogido: " + e.getLocalizedMessage());

			}
		}
	}
	
	private void removeUser(String username) {
		bd.removeUser(username);
		try {
			fout.writeObject(new MensajeConfCerrarConexion());
			fout.flush();
		} catch (IOException e) {
			System.out.println("Problema al enviar mensaje de confirmación para cerrar la conexión: " + e.getLocalizedMessage());

		}
	}
	
	private void pedirArchivo(String filename, String user) {
		String owner = bd.getOwner(filename);
		if(owner == null) {
			try {
				fout.writeObject(new MensajeArchivoNoExiste());
				fout.flush();
			} catch (IOException e) {
				System.out.println("Problema al enviar mensaje de archivo no existe " + e.getLocalizedMessage());
			}
		} else {
			ObjectOutputStream auxout = bd.getUserFout(owner);
			
			try {
				auxout.writeObject(new MensajeEmitirFichero(filename, user));
				auxout.flush();
			} catch (IOException e) {
				System.out.println("Problema al enviar mensaje para emitir fichero " + e.getLocalizedMessage());
			}
		}
		
	}
	
	private void emisorPreparado(String user, String host, int port) {
		ObjectOutputStream auxout= bd.getUserFout(user);
		try {
			auxout.writeObject(new MensajeEmisorPreparadoSC(host, port));
			auxout.flush();
		} catch (IOException e) {
			System.out.println("Problema al mandar MensajeEmisorPreparado del servidor al cliente receptor: " + e.getLocalizedMessage());
		}
		
	}
	
	private void closeAll() {
		try {
			fin.close();
		} catch (IOException e) {
			System.out.println("No se ha podido cerra el canal del os: " + e.getLocalizedMessage());
		}   
        try {
			s.close();
		} catch (IOException e) {
			System.out.println("No se ha podido cerra el socket del os: " + e.getLocalizedMessage());
		}

	}
}
