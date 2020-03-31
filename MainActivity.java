/*
	cryptochat Android app uses a symmetric key for 
	encryption/decryption and is designed to be 
	completely anonymous.

	Copyright (c) 2015 GB Tony Cabrera

	NOTE: You must provide your own encrypt/decrypt algorithms
*/

package com.example.app1114;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.Context;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.MenuInflater;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.String;
import java.nio.charset.Charset;



public class MainActivity extends Activity
{
	public static final String APP_NAME = "crypto_chat";
	//private static final String DEST = "192.168.1.102";
	private static final String DEST = "192.168.1.221";
	public static final int PORT = 6969;
	
	static byte[] hex_key = null;

	static int m_id = 0;
	static String SERVER_IP = "";
	static String FROM = "Alice";	//FIXME
	static String TO = "Bob";

	String MSG_FILE = "crypto.txt";
	String KEY_FILE = "key.txt";


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		

// READ STORED KEY
		EditText edit_key = (EditText)findViewById(R.id.edit_key);
		TextView text_hash = (TextView)findViewById(R.id.text_hash);
		int l = 0;
		try {
    		File sdcard = Environment.getExternalStorageDirectory();
    		File my_key = new File(sdcard,KEY_FILE);
			if(my_key.exists()){
				FileInputStream fis = new FileInputStream(my_key);
				byte fileContent[] = new byte[(int)my_key.length()];
				fis.read(fileContent);
     			fis.close();
				l = (int)my_key.length();
				hex_key = fileContent;
				edit_key.setHint("Key accepted.");
			}
 		}catch (IOException e) {
    		e.printStackTrace();           
 		}
		String key_hash = get_hex(hex_key);
		text_hash.setText(key_hash);

//FIXME		TextView text_local_ip = (TextView)findViewById(R.id.text_local_ip);
//FIXME        String ip = getLocalIpAddress();
//FIXME        text_local_ip.setText(ip);

//FIXME		EditText edit_dest_ip = (EditText)findViewById(R.id.edit_dest_ip);
//FIXME		edit_dest_ip.setText(DEST);

// SET TO
		EditText edit_to = (EditText)findViewById(R.id.edit_to);
		edit_to.setText(TO);

// READ CRYPTO
		try {
    		File sdcard = Environment.getExternalStorageDirectory();
    		File my_msg = new File(sdcard,MSG_FILE);

			if(my_msg.exists()){
    			BufferedReader br = new BufferedReader(new FileReader(my_msg));  
    			String line;   
    			while ((line = br.readLine()) != null) {
					addTextView(line);
				}
     			br.close();
			}
 		}catch (IOException e) {
    		e.printStackTrace();           
 		}

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_action_bar, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_settings:
				return true;
			case R.id.action_about:
				showAbout();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void showAbout(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String text = "Version " + getResources().getString(R.string.app_version) + "\n" + getResources().getString(R.string.about_text);
		builder.setTitle(R.string.about_title)
			.setMessage(text)
			.setPositiveButton("Close", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});

		

		AlertDialog about = builder.create();
		about.show();
	}

	public static String getLocalIpAddress() {
        try {
            NetworkInterface net = NetworkInterface.getByName("wlan0");
            String if_name = net.getName();
            Log.d(APP_NAME, if_name);
            Enumeration<InetAddress> inetAddresses = net.getInetAddresses();
            String ip_address="";
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                ip_address = inetAddress.getHostAddress();
                Log.d(APP_NAME, ip_address);
            }
            return ip_address;
        } catch (SocketException e) {
            Log.e(APP_NAME, e.toString());
        }
        return null;
    }
/*	
	public void setIP(View view){
		EditText edit_dest_ip = (EditText)findViewById(R.id.edit_dest_ip);
		TextView text_dest_ip = (TextView)findViewById(R.id.text_dest_ip);
		SERVER_IP = edit_dest_ip.getText().toString();
		text_dest_ip.setText(SERVER_IP + ":" + PORT);
		edit_dest_ip.setText("");
		edit_dest_ip.setHint("Destination accepted.");
		hideSoftKeyboard(this);
	}
*/
	public void makeKey(View view){
		EditText edit_key = (EditText)findViewById(R.id.edit_key);
		TextView text_hash = (TextView)findViewById(R.id.text_hash);
		String plain_key = edit_key.getText().toString();
		Key key = new Key(plain_key);
		hex_key = key.getHashBytes();
		text_hash.setText(key.getHashString());
		edit_key.setText("");
		edit_key.setHint("Key accepted.");
		hideSoftKeyboard(this);
		// Write to file
		Log.d(APP_NAME, "Writing key to file...");
		try {
			File sdcard = Environment.getExternalStorageDirectory();
			File my_file = new File(sdcard, KEY_FILE);
			FileOutputStream fos = new FileOutputStream(my_file);
			byte[] key_bytes = key.getHashBytes();
			//fos.write(key_bytes.getBytes()); //FIXME
			fos.write(key_bytes); //FIXME
			fos.close();
           	Log.d(APP_NAME, "Done writing.");
       	} catch (FileNotFoundException e) {
           	Log.e(APP_NAME, e.toString());
       	} catch (IOException e) {
           	Log.e(APP_NAME, e.toString());
       	}
		// Done writing
	}


	public void send(View view){
		EditText edit_message = (EditText)findViewById(R.id.edit_message);
//FIXME		EditText edit_from = (EditText)findViewById(R.id.edit_from);
		EditText edit_to = (EditText)findViewById(R.id.edit_to);
		Message message = new Message(edit_message.getText().toString(), m_id);
//FIXME		FROM = edit_from.getText().toString().trim();
		TO = edit_to.getText().toString().trim();
		if(hex_key != null){
		if(message.length() != 0){
			byte[] cipher_bytes = message.encrypt(hex_key);
//FIXME			send_cipher(cipher_bytes);
			String ciphertext = get_hex(cipher_bytes);
			addTextView(m_id + ": " + ciphertext);
//			addTextView(m_id, message.toString());
			addTextView(m_id + ": " + message.toString());
			
			// Write to file
            Log.d(APP_NAME, "Writing message to file...");
			try {
				File sdcard = Environment.getExternalStorageDirectory();
				File my_file = new File(sdcard, MSG_FILE);
				FileOutputStream fos = new FileOutputStream(my_file, true);
				//FileOutputStream fos = openFileOutput(MSG_FILE, Context.MODE_APPEND);
				//FileOutputStream fos = openFileOutput(MSG_FILE, Context.MODE_PRIVATE);
				String msg = m_id + ": " + message.toString() + "\n";
				fos.write(msg.getBytes());
				fos.close();
            	Log.d(APP_NAME, "Done writing.");
        	} catch (FileNotFoundException e) {
            	Log.e(APP_NAME, e.toString());
        	} catch (IOException e) {
            	Log.e(APP_NAME, e.toString());
        	}
			// Done writing
			
			m_id++;
			if(m_id == 100){
				m_id = 0;
			}
		}
		}
		edit_message.setText("");
		hideSoftKeyboard(this);
	}

	public void send_cipher(byte[] bytes){
		Thread cWrite = new Thread(new ClientWrite(bytes));
		cWrite.start();
	}

//	public void addTextView(int id, String text){
	public void addTextView(String text){
	
		LinearLayout layout_chat = (LinearLayout)findViewById(R.id.layout_chat);
		
		// Blue line	
		TextView bar = new TextView(this);
		bar.setBackgroundColor(0xff00ddff);
		bar.setHeight(1);
		layout_chat.addView(bar);

		//Text
		TextView v = new TextView(this);
//		v.setText(id + ": " + text);
		v.setText(text);
		layout_chat.addView(v);
		
		//Spacer
		TextView bar2 = new TextView(this);
		bar2.setHeight(25);
		layout_chat.addView(bar2);

		ScrollView scrollview = (ScrollView)findViewById(R.id.scroll_chat);
		scrollview.fullScroll(ScrollView.FOCUS_DOWN);
	}

	public static String get_hex(byte[] bytes){
			
		String s;
		StringBuilder sb = new StringBuilder(2*bytes.length);
		for(byte b : bytes){
			sb.append(String.format("%02x", b&0xff));
		}
		s = sb.toString();
		return s;
	}
	public static void hideSoftKeyboard(Activity activity) {
    	InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
	}
	
}
