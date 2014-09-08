/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import echoclient.EchoClient;
import echoclient.EchoListener;
import echoserver.EchoServer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Lars Mortensen
 */
public class TestClient {

    static String msg;

    public TestClient() {
    }

    @BeforeClass
    public static void setUpClass() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EchoServer.main(null);
                } catch (IOException ex) {
                    Logger.getLogger(TestClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    @AfterClass
    public static void tearDownClass() {
        EchoServer.stopServer();
    }

    @Before
    public void setUp() {
    }

    @Test
    public void send() throws IOException, InterruptedException {
        EchoClient client = new EchoClient();

        EchoListener listen = new EchoListener() {

            @Override
            public void messageArrived(String data) {
                msg = data;
            }
        };
        client.connect("localhost", 9090);
        client.registerEchoListener(listen);
        client.start();
        client.send("Hello");
        Thread.sleep(10);

        assertEquals("HELLO", msg);
    }

}
