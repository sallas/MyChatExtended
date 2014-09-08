package echoserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;
import utils.Utils;

public class EchoServer {

    private static boolean keepRunning = true;
    private static ServerSocket serverSocket;
    private static final Properties properties = Utils.initProperties("server.properties");
    private static List<ClientHandler> clientHandlers;
    private static List<String> nickNames;

    public EchoServer() {
        nickNames = new ArrayList<>();
        clientHandlers = new ArrayList<>();
        int port = Integer.parseInt(properties.getProperty("port"));
        String ip = properties.getProperty("serverIp");
        String logFile = properties.getProperty("logFile");
        Utils.setLogFile(logFile, EchoServer.class.getName());
        Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Sever started");
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip, port));
            do {
                Socket socket = serverSocket.accept(); //Important Blocking call
                Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Connected to a client");
                ClientHandler ch = new ClientHandler(socket, this);
                ch.start();
                clientHandlers.add(ch);
            } while (keepRunning);
        } catch (IOException ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Utils.closeLogger(EchoServer.class.getName());
        }
    }

    public static void stopServer() {
        keepRunning = false;
    }

    public void removeHandler(ClientHandler ch) {
        nickNames.remove(ch.getNickName());
        clientHandlers.remove(ch);
        sendNickNameList();
    }

    public void send(String message, ClientHandler handler) {
        for (ClientHandler ch : clientHandlers) {
            ch.send(message);
        }
    }

    public static void main(String[] args) {
        EchoServer server = new EchoServer();
    }

    private void sendNickNameList() {
        String names = "#list";
        for (String s : nickNames) {
            System.out.println(s);
            names += s + "-";
        }
        for (ClientHandler ch : clientHandlers) {
            ch.send(names);
        }
    }

    public void addNickName(String string) {
        nickNames.add(string);
        sendNickNameList();
    }
}
