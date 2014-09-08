package webserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import echoserver.EchoServer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import utils.Utils;

public class WebServer {

    static int port = 9090;
    static String ip = "127.0.0.1";
    static String contentFolder = "public/";
    private static final Properties properties = Utils.initProperties("server.properties");
    private static EchoServer echoServer;

    public static void main(String[] args) throws IOException {
        WebServer server = new WebServer(null);
    }

    public WebServer(EchoServer echo) throws IOException {

        //ip = properties.getProperty("serverIp");
        //port = Integer.parseInt(properties.getProperty("port"));
        echoServer = echo;
        InetSocketAddress i = new InetSocketAddress(ip, port);
        HttpServer server = HttpServer.create(i, 0);
        server.createContext("/welcome", new WelcomeHandler());
        server.createContext("/headers", new HeaderHandler());
        server.createContext("/pages", new PagesHandler());
        server.createContext("/parameters", new ParameterHandler());
        server.createContext("/users", new UsersHandler());
        server.createContext("/log", new LogHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Started the server, listening on: " + port);
        System.out.println(ip);
    }

    static class LogHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String response;
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html>\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<title>My fancy Web Site</title>\n");
            sb.append("<meta charset='UTF-8'>\n");
            sb.append("</head>\n");
            sb.append("<body>\n");

            Scanner scan = new Scanner(new FileReader(properties.getProperty("logFile")));
            while (scan.hasNextLine()) {
                String string = scan.nextLine();
                sb.append("<div>");
                sb.append(string);
                sb.append("</div>");
            }

            sb.append("</body>\n");
            sb.append("</html>\n");

            response = sb.toString();
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "text/html");
            he.sendResponseHeaders(200, response.length());
            try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                pw.print(response);
            }
        }

    }

    static class UsersHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String response;
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html>\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<title>My fancy Web Site</title>\n");
            sb.append("<meta charset='UTF-8'>\n");
            sb.append("</head>\n");
            sb.append("<body>\n");

            List<String> names = echoServer.getNickNames();
            for (String string : names) {
                sb.append("<div>");
                sb.append(string);
                sb.append("</div>");
            }

            sb.append("</body>\n");
            sb.append("</html>\n");

            response = sb.toString();
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "text/html");
            he.sendResponseHeaders(200, response.length());
            try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                pw.print(response);
            }
        }

    }

    static class ParameterHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String response;
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html>\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<title>My fancy Web Site</title>\n");
            sb.append("<meta charset='UTF-8'>\n");
            sb.append("</head>\n");
            sb.append("<body>\n");
            sb.append("<div>");
            sb.append(he.getRequestMethod());
            if (he.getRequestMethod().equalsIgnoreCase("GET")) {
                sb.append(he.getRequestURI().getQuery());
            } else {
                Scanner scan = new Scanner(he.getRequestBody());
                while (scan.hasNext()) {
                    sb.append("Request body, with Post-parameters: " + scan.nextLine());
                    sb.append("</br>");
                }
            }
            sb.append("</div>");
            sb.append("</body>\n");
            sb.append("</html>\n");

            response = sb.toString();
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "text/html");
            he.sendResponseHeaders(200, response.length());
            try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                pw.print(response);
            }
        }

    }

    static class PagesHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {

            String s = he.getRequestURI().toString();
            s = s.substring(6);
            File file = new File(contentFolder + s);
            byte[] bytesToSend = new byte[(int) file.length()];
            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                bis.read(bytesToSend, 0, bytesToSend.length);
            } catch (IOException ie) {
                ie.printStackTrace();
            }
            he.sendResponseHeaders(200, bytesToSend.length);
            try (OutputStream os = he.getResponseBody()) {
                os.write(bytesToSend, 0, bytesToSend.length);
            }
        }

    }

    static class HeaderHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String response;
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html>\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<title>My fancy Web Site</title>\n");
            sb.append("<meta charset='UTF-8'>\n");
            sb.append("</head>\n");
            sb.append("<body>\n");
            //table stuff
            Map<String, List<String>> m = he.getRequestHeaders();
            sb.append("<table border='1'>");
            for (Entry<String, List<String>> entry : m.entrySet()) {
                String s = entry.getKey();
                List<String> list = entry.getValue();
                sb.append("<tr>");
                sb.append("<td>");
                sb.append(s);
                sb.append("</td>");
                sb.append("<td>");
                for (String string : list) {
                    sb.append(string);
                    //sb.append(" , "); 
                }
                sb.append("</td>");
                //sb.replace(sb.length() - 3, sb.length() - 1, "");
                sb.append("</tr>");

            }
            sb.append("</table>");

            sb.append("");
            sb.append("</body>\n");
            sb.append("</html>\n");

            response = sb.toString();
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "text/html");
            he.sendResponseHeaders(200, response.length());
            try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                pw.print(response);
            }
        }

    }

    static class WelcomeHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            String response;
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html>\n");
            sb.append("<html>\n");
            sb.append("<head>\n");
            sb.append("<title>My fancy Web Site</title>\n");
            sb.append("<meta charset='UTF-8'>\n");
            sb.append("</head>\n");
            sb.append("<body>\n");
            sb.append("<h2>Welcome to my very first home made Web Server :-)</h2>\n");
            sb.append("</body>\n");
            sb.append("</html>\n");

            response = sb.toString();
            Headers h = he.getResponseHeaders();
            h.add("Content-Type", "text/html");
            he.sendResponseHeaders(200, response.length());
            try (PrintWriter pw = new PrintWriter(he.getResponseBody())) {
                pw.print(response);
            }

        }

    }

}
