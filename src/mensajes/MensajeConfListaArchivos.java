package mensajes;

import java.util.Map;

public class MensajeConfListaArchivos extends Mensaje{

	Map<String, String[]> archivos;
	
	public MensajeConfListaArchivos(Map<String, String[]> archivos) {
		tipo = MensType.MENSAJE_CONFIRMACION_LISTA_ARCHIVOS;
		this.archivos = archivos;
	}
	
	public Map<String, String[]> getArchivos() {
		return archivos;
	}
	
}
