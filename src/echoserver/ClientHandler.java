package echoserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;

public class ClientHandler extends Thread {

    private Scanner input;
    private PrintWriter writer;
    private Socket socket;
    private EchoServer server;
    private String nickName;

    public String getNickName() {
        return nickName;
    }

    public ClientHandler(Socket socket, EchoServer server) throws IOException {
        input = new Scanner(socket.getInputStream());
        writer = new PrintWriter(socket.getOutputStream(), true);
        this.socket = socket;
        this.server = server;

    }

    public void send(String message) {
        writer.println(message.toUpperCase());
        Logger.getLogger(EchoServer.class.getName()).log(Level.INFO,
                String.format("Received the message: %1$S ",
                        message.toUpperCase()));
    }

    public void run() {
        try {
            String message = input.nextLine(); //IMPORTANT blocking call

            if (message.length() > 8 && message.substring(0, 8).equalsIgnoreCase("#connect")) {
                String name = message.substring(8);
                server.addNickName(name);
                nickName = name;
                message = input.nextLine();
                while (!message.equals(ProtocolStrings.STOP)) {
                    Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, String.format("Received the message: %1$S ", message));
                    server.send(message, this);
                    message = input.nextLine(); //IMPORTANT blocking call
                }
            }

            writer.println(ProtocolStrings.STOP);//Echo the stop message back to the client for a nice closedown
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NoSuchElementException ex) {
            //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            server.removeHandler(this);
        }
        Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Closed a Connection");
    }
}
