package com.messaging;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class SendFile {
    String sendfile;

    String theip;

    public SendFile(String ip, String file) {
        theip = ip;
        sendfile = file;
    }

    void fileClient() {
        try {
            Socket client = new Socket(theip, 8888);
            BufferedInputStream f = new BufferedInputStream(
                    new FileInputStream(sendfile));
            PrintStream out = new PrintStream(client.getOutputStream(), true);
            byte[] buf = new byte[1024];
            int l = 0;
            while ((l = f.read(buf, 0, 1024)) != -1) {
                out.write(buf, 0, l);
            }

            f.close();
            out.close();
            client.close();
        } catch (IOException ioe) {
            System.err.println("Error " + ioe);
        }
    }

}