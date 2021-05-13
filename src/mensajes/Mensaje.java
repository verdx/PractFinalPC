package mensajes;

import java.io.Serializable;

public abstract class Mensaje implements Serializable{
	public MensType getTipo() {
		return tipo;
	}

	MensType tipo;
}
