package Project;

import Project.addons.Colors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
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
                String prefix = "/";
                String message = scanner.nextLine();

                if (message.startsWith(prefix)) {
                    String command = message.substring(1);

                    if (command.startsWith("nick")) {
                        String[] cmdParams = command.split("\\s", 2);
                        boolean startsWithWhitespace = Character.isWhitespace(cmdParams[1].charAt(0));
                        
                        if (cmdParams.length == 2 && !startsWithWhitespace) {
                            String nickname = cmdParams[1];

                            bufferredWriter.write(Colors.CYAN + name + " changed name to " + nickname + Colors.RESET);
                            bufferredWriter.newLine();
                            bufferredWriter.flush();

                            name = nickname;

                            System.out.println(Colors.CYAN + "Successfully changed name" + Colors.RESET);
                        }
                    }
                } else {
                    bufferredWriter.write("<" + name + "> " + message);
                    bufferredWriter.newLine();
                    bufferredWriter.flush();
                }
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

        InetAddress address;
        byte[] rawIP = new byte[4];
        String[] code;
        int port;

        System.out.println(Colors.RED + "\nThis instance will abort if the code is wrong" + Colors.RESET);

        System.out.print("Enter the server code given to you: ");
        code = scanner.nextLine().split("\\s+");
        port = Integer.parseInt(code[0]);

        for (int i=1; i<code.length; i++) {
            int temp = Integer.parseInt(code[i]);
            byte unsignedB = (byte) (temp & 0xff);
            rawIP[i-1] = unsignedB;
        }

        address = InetAddress.getByAddress(rawIP);

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        
        Socket socket = new Socket(address, port);
        Client client = new Client(socket, name);
        client.read();
        client.send();

        scanner.close();
    }
}