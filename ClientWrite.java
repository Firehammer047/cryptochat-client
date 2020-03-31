/*
	cryptochat Android app uses a symmetric key for 
	encryption/decryption and is designed to be 
	completely anonymous.

	Copyright (c) 2015 GB Tony Cabrera

	NOTE: You must provide your own encrypt/decrypt algorithms
*/

package com.example.app1114;

import android.util.Log;

import java.io.*;
import java.net.*;


public class ClientWrite implements Runnable {
	private static final String APP_NAME = MainActivity.APP_NAME;
	private String SERVER_IP = MainActivity.SERVER_IP;
	private static final int PORT = MainActivity.PORT;
	private String FROM = MainActivity.FROM;
	private String TO = MainActivity.TO;
	private byte[] bytes;
	private String header = "";
	private byte[] header_bytes;


	public ClientWrite(byte[] b){
		this.bytes = b;
	}

	public void run() {

		Log.d(APP_NAME, "Trying " + SERVER_IP + ":" + PORT);
		try{
			Socket s = new Socket(SERVER_IP, PORT);
			Log.d(APP_NAME, "Connected.");
			
			Log.d(APP_NAME, "From: " + FROM);
			Log.d(APP_NAME, "To: " + TO);
			
			header = FROM + ":" + TO + "#!";
			
			OutputStream os = s.getOutputStream();
			try{
				header_bytes = header.getBytes("UTF-8");
			} catch (UnsupportedEncodingException ex) {
			}

			short h_l = (short)header_bytes.length;
			short b_l = (short)bytes.length;
			short l = (short)(h_l + b_l);

			byte[] len = new byte[2];
			len[0] = (byte)(l & 0xff);
			len[1] = (byte)((l >> 8) & 0xff);
			Log.d(APP_NAME, "Sending bytes: " + l);
			os.write(len[0]);
			os.write(len[1]);
			// SEND HEADER
			os.write(header_bytes);
			os.write(bytes);
			os.flush();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			String reply = in.readLine();
			Log.d(APP_NAME, "Server reply: " + reply);
			
			Log.d(APP_NAME, "Message sent.");
			s.close();
		} catch (UnknownHostException e) {
			String st = Log.getStackTraceString(e);
		} catch (IOException e) {
			String st = Log.getStackTraceString(e);
		}
	}
}
