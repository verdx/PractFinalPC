package parte1;

public class MainParte1 {

	public static void main(String args[]) {
		int port = 2001;
		String [] archivos = {"prueba1.txt", "prueba2.txt", "prueba3.txt", "prueba4.txt", "prueba5.txt"};
		BaseDeDatos bd = new BaseDeDatos(archivos);
		Servidor servidor = new Servidor(bd, port);
		servidor.start();
		System.out.println("Empezamos con los clientes");
		for(int i = 1; i < 6; i++) {
			Cliente aux = new Cliente(archivos[i-1], port);
			aux.start();
		}
	}
}
