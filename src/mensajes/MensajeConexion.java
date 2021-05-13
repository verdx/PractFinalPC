package mensajes;

public class MensajeConexion extends Mensaje {
	
	String username;
	String[] files;
	
	public MensajeConexion(String username, String[] files) {
		tipo = MensType.MENSAJE_CONEXION;
		this.username = username;
		this.files = files;
	}
	
	public String getUsername() {
		return username;
	}

	public String[] getFiles() {
		return files;
	}
}
