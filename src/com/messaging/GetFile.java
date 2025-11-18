package com.messaging;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class GetFile {
	String receiveFile;

	String receivedir;

	public GetFile(String dir, String file) {
		receivedir = dir;
		receiveFile = file;
	}

	void fileServer() {
		try {
			ServerSocket server = new ServerSocket(8888);
			Socket incoming = server.accept();
			File file = new File(receivedir, receiveFile);
			BufferedOutputStream f = new BufferedOutputStream(
					new FileOutputStream(file));
			BufferedInputStream in = new BufferedInputStream(incoming
					.getInputStream());
			byte[] buf = new byte[1024];
			int l = 0;
			while ((l = in.read(buf, 0, 1024)) != -1) {
				f.write(buf, 0, l);
			}
			in.close();
			f.close();
			server.close();

		} catch (BindException be) {
			System.err.println("锟矫端匡拷锟窖憋拷占锟斤拷" + be.toString());
		} catch (IOException ioe) {
			System.err.println("I/O error - " + ioe);
		}
	}

}
