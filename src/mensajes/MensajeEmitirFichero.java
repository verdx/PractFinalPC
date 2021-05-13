package mensajes;

public class MensajeEmitirFichero extends Mensaje{
	
	String filename;
	String user;
	
	public MensajeEmitirFichero(String filename, String user) {
		this.filename = filename;
		this.user = user;
	}
	
	public String getFilename() {
		return filename;
	}

	public String getUser() {
		return user;
	}

}
