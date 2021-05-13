package mensajes;

public class MensajePedirFichero extends Mensaje{

	String filename;
	String user;
	
	public MensajePedirFichero(String filename, String user) {
		this.filename = filename;
		this.user = user;
		tipo = MensType.MENSAJE_PEDIR_FICHERO;
	}
	
	public String getFilename() {
		return filename;
	}

	public String getUser() {
		return user;
	}
}
