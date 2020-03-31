/*
	cryptochat Android app uses a symmetric key for 
	encryption/decryption and is designed to be 
	completely anonymous.

	Copyright (c) 2015 GB Tony Cabrera

	NOTE: You must provide your own encrypt/decrypt algorithms
*/

package com.example.app1114;

import java.io.*;
import java.security.*;

public class Key {
	private String k;
	private String digest;
	private byte[] hash;
	
	public Key(String k){
		this.k = k;
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			hash = md.digest(k.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder(2*hash.length);
			for(byte b : hash){
				sb.append(String.format("%02x", b&0xff));
			}
			digest = sb.toString();
		} catch (UnsupportedEncodingException e){
		} catch (NoSuchAlgorithmException e){
		}
	}
	public byte[] getHashBytes(){
		return hash;
	}

	public String getHashString(){
		return digest;
	}

	public String toString(){
		return k;
	}


}
