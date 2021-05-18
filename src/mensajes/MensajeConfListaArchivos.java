package mensajes;

import java.util.List;
import java.util.Map;

public class MensajeConfListaArchivos extends Mensaje{

	Map<String, List<String>> archivos;
	
	public MensajeConfListaArchivos(Map<String, List<String>> files) {
		tipo = MensType.MENSAJE_CONFIRMACION_LISTA_ARCHIVOS;
		this.archivos = files;
	}
	
	public Map<String, List<String>> getArchivos() {
		return archivos;
	}
	
}
