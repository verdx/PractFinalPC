package mensajes;

public class MensajeArchivosSubidos extends Mensaje{
	
	private int n_subidos_corr;
	
	public MensajeArchivosSubidos(int n) {
		tipo = MensType.MENSAJE_ARCHIVOS_SUBIDOS;
		this.n_subidos_corr = n;
	}
	
	public int getCorrectos() {
		return n_subidos_corr;
	}

}
