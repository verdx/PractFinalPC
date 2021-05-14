package mensajes;

public class MensajeEmitirFichero extends Mensaje{
	
	String filename;
	String user;
	
	public MensajeEmitirFichero(String filename, String user) {
		this.tipo = MensType.MENSAJE_EMITIR_FICHERO;
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
