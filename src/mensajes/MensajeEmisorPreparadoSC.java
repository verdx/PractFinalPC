package mensajes;

public class MensajeEmisorPreparadoSC extends Mensaje{

	String host;
	int port;
	
	public MensajeEmisorPreparadoSC(String host, int port) {
		tipo = MensType.MENSAJE_PREPARADO_EMISORSC;
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
}
