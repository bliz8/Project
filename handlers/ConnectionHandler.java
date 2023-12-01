package Project.handlers;

import Project.addons.Colors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionHandler implements Runnable {
    public static CopyOnWriteArrayList<ConnectionHandler> connectionHandlers = new CopyOnWriteArrayList<>();
    public Socket socket;
    private BufferedReader bufferReader;
    private BufferedWriter bufferWriter;
    private String name;

    public ConnectionHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferReader = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
            this.bufferWriter = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()) );
            this.name = bufferReader.readLine();

            connectionHandlers.add(this);

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
                break;
            }
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            try {
                if (!connectionHandler.name.equals(name)) {
                    connectionHandler.bufferWriter.write(message);
                    connectionHandler.bufferWriter.newLine();
                    connectionHandler.bufferWriter.flush();
                }
            } catch (IOException e) {
                close(socket, bufferReader, bufferWriter);
            }
        }
    }

    public void removeConnectionHandler() {
        connectionHandlers.remove(this);
        broadcast(Colors.RED + "[SERVER] " + name + " has left." + Colors.RESET);
    }

    public void close(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeConnectionHandler();
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}