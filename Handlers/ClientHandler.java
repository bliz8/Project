package Project.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
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

            clientHandlers.add(this);

            broadcast("[SERVER] " + name + " has joined.");
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
        for (ClientHandler clientHandler : clientHandlers) {
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

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcast("[SERVER] " + name + " has left.");
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