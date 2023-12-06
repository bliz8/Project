package Project;

import Project.addons.Colors;
import Project.handlers.ConnectionHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Scanner;

/**
 * The {@code Server} class represents a simple server application that accepts incoming client connections.
 * It creates a ServerSocket, listens for client connections on a specified port, and handles each
 * client connection using a separate thread.
 * 
 * @author Troy Vu {@link https://github.com/bliz8/Project}
 * @author WittCode {@link https://www.youtube.com/@WittCode}
 * @see {@link https://www.youtube.com/watch?v=gLfuZrrfKes} by WittCode
 */
public class Server {
    private ServerSocket serverSocket;
    private ExecutorService pool;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        pool = Executors.newCachedThreadPool();
        
        System.out.println(Colors.GREEN + "\nServer created." + Colors.RESET);

        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println(Colors.YELLOW + "User connected." + Colors.RESET);

                ConnectionHandler clientHandler = new ConnectionHandler(socket);
                pool.execute(clientHandler);
            }

        } catch (IOException e) {
            
        }

        scanner.close();
    }

    public void close() {
        try {
            pool.shutdown();
            if (serverSocket != null) serverSocket.close();

            for (ConnectionHandler connectionHandler : ConnectionHandler.connectionHandlers) {
                connectionHandler.close();
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

        System.out.print("Server Code: " + Colors.WHITE_BG + " " + port);
        for (byte b : ip) System.out.print(" " + b);
        System.out.print(" " + Colors.RESET);

        server.start();
    }
}