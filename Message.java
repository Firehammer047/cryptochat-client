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

public class Message {
	private static final String APP_NAME = MainActivity.APP_NAME;
	private String m;
	private int id;
	private byte[] m_bytes;
	private byte[] c_bytes;
	
	public Message(String m, int id){
		this.m = m;
		this.id = id;
		try{
			m_bytes = m.getBytes("UTF-8");
		} catch (UnsupportedEncodingException ex) {
		}
	}
	public Message(byte[] c_bytes){
		this.c_bytes = c_bytes;
	}
	private byte[] assemble(){
		short len = (short)m_bytes.length;
		byte[] new_m = new byte[len + 3];
		
		// Convert id and length to first 3 bytes of message
		new_m[0] = (byte)id;
		new_m[1] = (byte)(len & 0xff);
		new_m[2] = (byte)((len >> 8) & 0xff);

		// Copy original bytes	
		for(int i=0; i < len; i++){
			new_m[i+3] = m_bytes[i];
		}
		return new_m;
	}
	public byte[] encrypt(byte[] key){
		Log.d(APP_NAME, "Encrypting...");
		byte[] m_bytes_id = assemble();
		byte[] m_bytes_pad = pad_message(m_bytes_id, key);
		int pad_len = m_bytes_pad.length;
		byte[] cipher_bytes = new byte[pad_len];
/*
		Start Algorithm ------------
		
		Put your own encryption algorithm here.				
		
		End Algorithm ---------------
*/
		Log.d(APP_NAME, "encrypt() cipher_bytes: " + MainActivity.get_hex(cipher_bytes));
			
		return cipher_bytes;

	}

	private byte[] pad_message(byte[] m, byte[] k){
		int r = 0;
		int padding = 0;
		int len = m.length;
		

		r = len % 16;
		if(r > 0){
			padding = 16 - r;
		}
		int new_len = len + padding;
		byte[] new_m = new byte[new_len];
		
		// Copy original bytes
		for(int i=0; i<len; i++){
			new_m[i] = m[i];
		}
		
		// Pad with key bytes
		for(int i=len, j=0; i<new_len; i++, j++){
			new_m[i] = k[j];
		}
		return new_m;
	}

	public byte[] decrypt(byte[] key){
		
		int len = c_bytes.length;
		byte[] plain_bytes_pad = new byte[len];
		
/*
		Start Algorithm ------------
		
		Put your own decryption algorithm here.				
		
		End Algorithm ---------------
*/
		
		byte[] plain_bytes = unpad_message(plain_bytes_pad);
		return plain_bytes;
	}
	
	public byte[] unpad_message(byte[] m){
		
		this.id = m[0];

		byte[] len = new byte[2];
		len[0] = m[1];
		len[1] = m[2];

		short m_len = (short)((len[2] << 8) | (len[1] & 0xFF));
		
		byte[] orig_m = new byte[m_len];
		for(int i=0; i < m_len; i++){
			orig_m[i] = m[i+3];
		}
		return orig_m;
	}
	
	public int getId(){
		return id;
	}

	public String toString(){
		return m;
	}
	
	public int length(){
		return m.length();
	}

}
