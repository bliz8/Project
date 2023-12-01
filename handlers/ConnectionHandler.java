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
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;

    public ConnectionHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
            this.bufferedWriter = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()) );
            this.name = bufferedReader.readLine();

            connectionHandlers.add(this);

            broadcast(Colors.GREEN + "[SERVER] " + name + " has joined." + Colors.RESET);
        } catch (IOException e) {
            close();
        }
    }

    @Override
    public void run() {
        String message;

        while (socket.isConnected()) {
            try {
                message = bufferedReader.readLine();
                broadcast(message);
            } catch (IOException e) {
                close();
                break;
            }
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            try {
                if (!connectionHandler.name.equals(name)) {
                    connectionHandler.bufferedWriter.write(message);
                    connectionHandler.bufferedWriter.newLine();
                    connectionHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                close();
            }
        }
    }

    public void removeConnectionHandler() {
        connectionHandlers.remove(this);
        broadcast(Colors.RED + "[SERVER] " + name + " has left." + Colors.RESET);
    }

    public void close() {
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