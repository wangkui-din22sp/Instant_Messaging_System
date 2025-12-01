// ConnectionTest.java
package com.messaging;

import java.net.*;

public class ConnectionTest {
    public static void main(String[] args)  {
        String server = "localhost"; // Test locally first
        int port = 8080;
        
        System.out.println("Testing LOCAL connection to " + server + ":" + port);
        
        try {
            Socket socket = new Socket(server, port);
            System.out.println("✓ SUCCESS: Connected to LOCAL server!");
            socket.close();
        } catch (Exception e) {
            System.out.println("✗ FAILED locally: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

