package Project.handlers;

import Project.addons.Colors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> ClientHandlers = new ArrayList<>();
    public Socket socket;
    private BufferedReader bufferReader;
    private BufferedWriter bufferWriter;
    private String name;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferReader = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
            this.bufferWriter = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()) );
            this.name = bufferReader.readLine();

            ClientHandlers.add(this);

            broadcast(Colors.GREEN + "[SERVER] " + name + " has joined." + Colors.RESET);
        } catch (IOException e) {
            close(socket, bufferReader, bufferWriter);
        }
    }

    @Override
    public void run() {
        String message;

        while (socket.isConnected()) {
            try {
                message = bufferReader.readLine();
                broadcast(message);
            } catch (IOException e) {
                close(socket, bufferReader, bufferWriter);
            }
        }
    }

    public void broadcast(String message) {
        for (ClientHandler ClientHandler : ClientHandlers) {
            try {
                if (!ClientHandler.name.equals(name)) {
                    ClientHandler.bufferWriter.write(message);
                    ClientHandler.bufferWriter.newLine();
                    ClientHandler.bufferWriter.flush();
                }
            } catch (IOException e) {
                close(socket, bufferReader, bufferWriter);
            }
        }
    }

    public void removeClientHandler() {
        ClientHandlers.remove(this);
        broadcast(Colors.RED + "[SERVER] " + name + " has left." + Colors.RESET);
    }

    public void close(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}