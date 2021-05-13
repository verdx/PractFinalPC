package parte2;

public class MainCliente {

	public static void main(String[] args) {
		String [] filenames = {"prueba1.txt", "prueba2.txt", "prueba3.txt", "prueba4.txt", "prueba5.txt"};
		Cliente cliente = new Cliente(filenames);
		cliente.start();
	}
}
