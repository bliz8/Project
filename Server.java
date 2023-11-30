package Project;

import Project.addons.Colors;
import Project.handlers.ClientHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void start() {
        System.out.println(Colors.GREEN + "\nServer created." + Colors.RESET);

        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println(Colors.YELLOW + "User connected." + Colors.RESET);

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

        InetAddress serverAddress = InetAddress.getLocalHost();
        byte[] ip = serverAddress.getAddress();

        ServerSocket serverSocket = new ServerSocket(port, 0, serverAddress);
        Server server = new Server(serverSocket);

        System.out.print("Server Code: " + port);
        for (byte b : ip) System.out.print(" " + b);

        server.start();
    }
}