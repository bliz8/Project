package Project.handlers;

import Project.addons.Colors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler implements Runnable {
    public static CopyOnWriteArrayList<ClientHandler> ClientHandlers = new CopyOnWriteArrayList<>();
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
        for (ClientHandler clientHandler : ClientHandlers) {
            if (!clientHandler.socket.isConnected()) {
                try {
                    clientHandler.bufferWriter.write(message);
                    clientHandler.bufferWriter.newLine();
                    clientHandler.bufferWriter.flush();
                    close(socket, bufferReader, bufferWriter);
                } catch (IOException e) {
                    close(socket, bufferReader, bufferWriter);
                }
            } else {
                try {
                    if (!clientHandler.name.equals(name)) {
                        clientHandler.bufferWriter.write(message);
                        clientHandler.bufferWriter.newLine();
                        clientHandler.bufferWriter.flush();
                    }
                } catch (IOException e) {
                    close(socket, bufferReader, bufferWriter);
                }
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