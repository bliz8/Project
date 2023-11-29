package Project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferredReader;
    private BufferedWriter bufferredWriter;
    private String name;

    public Client(Socket socket, String name) {
        try {
            this.socket = socket;
            this.bufferredWriter = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()) );
            this.bufferredReader = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
            this.name = name;
        } catch (IOException e) {
            close(socket, bufferredReader, bufferredWriter);
        }
    }

    public void send() {
        Scanner scanner = new Scanner(System.in);

        try {
            bufferredWriter.write(name);
            bufferredWriter.newLine();
            bufferredWriter.flush();

            while (socket.isConnected()) {
                String message = scanner.nextLine();
                bufferredWriter.write("<" + name + "> " + message);
                bufferredWriter.newLine();
                bufferredWriter.flush();
            }
        } catch (IOException e) {
            scanner.close();
            close(socket, bufferredReader, bufferredWriter);
        }
    }

    public void read() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String message;

                    while (socket.isConnected()) {
                        message = bufferredReader.readLine();
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    close(socket, bufferredReader, bufferredWriter);
                }
            }
        };

        new Thread(runnable).start();
    }

    public void close(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        int port;

        boolean valid = false;

        do {
            System.out.print("Enter the port # (0 to 65535): ");
            port = scanner.nextInt();

            if (port >= 0 && port <= 65535) valid = true;
        } while (!valid);
        
        Socket socket = new Socket("localhost", port);
        Client client = new Client(socket, name);
        client.read();
        client.send();

        scanner.close();
    }
}