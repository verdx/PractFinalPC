package mensajes;

public class MensajeCerrarConexion extends Mensaje{
	
	private String username;
	
	public MensajeCerrarConexion(String username) {
		tipo = MensType.MENSAJE_CERRAR_CONEXION;
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}

}
