package mensajes;

import java.util.List;

public class MensajeConexion extends Mensaje {
	
	String username;
	List<String> files;
	
	public MensajeConexion(String username, List<String> files) {
		tipo = MensType.MENSAJE_CONEXION;
		this.username = username;
		this.files = files;
	}
	
	public String getUsername() {
		return username;
	}

	public List<String> getFiles() {
		return files;
	}
}
