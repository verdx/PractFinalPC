package mensajes;

public class MensajeConfListaUsuarios extends Mensaje{

	String[] users;
	
	public MensajeConfListaUsuarios(String[] users) {
		tipo = MensType.MENSAJE_CONFIRMACION_LISTA_USUARIOS;
		this.users = users;
	}
	
	public String[] getUsers() {
		return users;
	}
}
