package parte2;

public class MainServidor {

	public static void main(String[] args) {
		BaseDeDatos bd = new BaseDeDatos();
		Servidor servidor = new Servidor(bd);
		servidor.start();
		try {
			servidor.join();
		} catch (InterruptedException e) {
			System.out.println("Problema al cerrar el servidor");
			e.printStackTrace();
		}
	}
}
