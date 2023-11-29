package Project;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void start() {
        System.out.println("Server created.");

        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("User connected.");

                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {

        }
    }

    public void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
                System.out.println("User disconnected.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        int port;

        Scanner scanner = new Scanner(System.in);
        boolean valid = false;

        do {
            System.out.print("Enter a port # (0 to 65535): ");
            port = scanner.nextInt();

            if (port >= 0 && port <= 65535) valid = true;
        } while (!valid);
        
        scanner.close();

        ServerSocket serverSocket = new ServerSocket(port);
        Server server = new Server(serverSocket);
        server.start();
    }
}