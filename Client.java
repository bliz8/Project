package Project;

import Project.addons.Colors;
import Project.addons.Settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * The {@code Client} class represents a simple client application that can connect to a server,
 * send messages, and receive messages in a chat-like communication.
 * 
 * @author Troy Vu {@link https://github.com/bliz8/Project}
 * @author WittCode {@link https://www.youtube.com/@WittCode}
 * @author NeuralNine {@link https://www.youtube.com/@NeuralNine}
 * @see {@link https://www.youtube.com/watch?v=gLfuZrrfKes} by WittCode
 * @see {@link https://www.youtube.com/watch?v=hIc_9Wbn704} by NeuralNine
 */
public class Client {
    private static int numOfClients = 0;
    private static int numOfGuests = 0;
    private boolean isHost;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;

    public Client(Socket socket, String name) {
        synchronized (Client.class) {
            try {
                this.bufferedReader = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
                this.bufferedWriter = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()) );
                this.socket = socket;
            } catch (IOException e) {
                close();
            }

            if (name.equals("Guest")) {
                ++numOfClients;
                ++numOfGuests;
                this.name = name + "_" + numOfGuests;
            } else {
                ++numOfClients;
                this.name = name;
            }

            this.isHost = numOfClients == 1;
        }
    }

    public void getNum() {
        System.out.println(numOfClients);
        System.out.println(numOfGuests);
    }

    public void send() {
        Scanner scanner = new Scanner(System.in);

        try {
            bufferedWriter.write(name);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            while (socket.isConnected()) {
                String message = scanner.nextLine();

                if (message.startsWith(Settings.PREFIX)) {
                    String command = message.substring(1);

                    if (isHost) {
                        if (command.startsWith("test")) {
                            System.out.println("You are the host! " + numOfClients);
                        }
                    }

                    if (command.startsWith("nick")) {
                        String[] cmdParams = command.split("\\s", 2);
                        boolean startsWithWhitespace = Character.isWhitespace(cmdParams[1].charAt(0));
                        
                        if (cmdParams.length == 2 && !startsWithWhitespace) {
                            String nickname = cmdParams[1];
                            nickname = nickname.replace(" ", "_");

                            bufferedWriter.write(Colors.CYAN + name + " changed name to " + nickname + Colors.RESET);
                            bufferedWriter.newLine();
                            bufferedWriter.flush();

                            name = nickname;

                            System.out.println(Colors.CYAN + "Successfully changed name" + Colors.RESET);
                        }
                    }

                    if (command.startsWith("viewtag")) {
                        System.out.println("<" + name + ">");
                    }
                } else {
                    if (message.length() > 0) {
                        bufferedWriter.write("<" + name + "> " + message);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }  
                }
            }
        } catch (IOException e) {
            scanner.close();
            close();
        }
    }

    public void read() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String message;

                    while (socket.isConnected()) {
                        message = bufferedReader.readLine();
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    close();
                }
            }
        };

        new Thread(runnable).start();
    }

    public void close() {
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

        System.out.println(Colors.RED + "\n[WARNING] This instance will abort if the code is wrong" + Colors.RESET);
        System.out.println(Colors.YELLOW + "[NOTE] First person joined will be the host\n" + Colors.RESET);

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
        name = name.replace(" ", "_");

        if (name.equals("")) name = "Guest";
        
        try {
            Socket socket = new Socket(address, port);
            Client client = new Client(socket, name);

            client.getNum();
            client.read();
            client.send();
        } catch (UnknownHostException e) {
            System.err.println(Colors.RED + "[UNKNOWN HOST] " + address + Colors.RESET);
        } catch (IOException e) {
            System.err.println(Colors.RED + "[CONNECTION ERROR] " + e.getMessage() + Colors.RESET);
        } finally {
            scanner.close();
        }
    }
}