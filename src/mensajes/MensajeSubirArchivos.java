package mensajes;

import java.util.List;

public class MensajeSubirArchivos extends Mensaje{
	
	List<String> filenames;
	
	public MensajeSubirArchivos(List<String> list) {
		tipo = MensType.MENSAJE_SUBIR_ARCHIVOS;
		this.filenames = list;
	}
	
	public List<String> getFileNames() {
		return filenames;
	}

}
