package parte2;

public class MainServidor {

	public static void main(String[] args) {
		BaseDeDatos bd = new BaseDeDatos();
		Servidor servidor = new Servidor(bd);
		servidor.start();
	}
}