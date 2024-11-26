import java.io.*;
import java.net.*;

public class Servidor {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            System.out.println("Servidor iniciado. Esperando conexión...");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    String mensajeCliente = input.readLine(); 
                    if (mensajeCliente != null) {
                        System.out.println("Se fue el Cliente " + mensajeCliente);
                    }
                } catch (IOException e) {
                    System.out.println("Error al procesar la conexión de un cliente.");
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
