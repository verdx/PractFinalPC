package mensajes;

public class MensajeEmisorPreparadoCS extends Mensaje{

	String host;
	int port;
	
	String user;
	
	public MensajeEmisorPreparadoCS(String host, int port, String user) {
		tipo = MensType.MENSAJE_PREPARADO_EMISORCS;
		this.host = host;
		this.port = port;
		this.user = user;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	
	public String getUser() {
		return user;
	}
}
