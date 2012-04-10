package edu.ucsb.deepspace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;

public class CommGalil implements CommInterface {
	//Singleton
	//private static final CommGalil INSTANCE = new CommGalil();
	//public static CommGalil getInstance() {return INSTANCE;}
	
	PrintWriter out;
	BufferedReader in;
	Socket socket = null;
	boolean connection = false;
	
	public CommGalil(int port) {
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress("192.168.1.200", port), 3000);
			socket.setSoTimeout(3000);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			connection = true;
			//watcher = new Watcher(out, in);
			//watcher.makeTimers();
			Stage.getInstance().confirmCommConnection();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			System.out.println("Could not connect to Galil.");
			connection = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Send message through output stream.
    public void send(String message) {
    	//Date date = new Date();
    	//watcher.send(message, date);
    	out.println(message);
    }
    
    public String read() {
    	String result = "";
    	//As long as we haven't reached the EOL character (:), continue looping.
    	while (!result.contains(":")) {
    		try {
				result += (char) in.read();
			} catch (SocketTimeoutException e1) {
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	//Deal with error message, if necessary.
    	if (result.contains("?")) {
    		System.out.println("Syntax Error");
    		System.out.println("Returned value: " + result);
    		System.out.println("Error code: " + sendRead("TC1"));
    	}
    	//System.out.println("length before trim:  " + result.length());
    	result = result.replace("\r\n:", "");
    	//System.out.println("length after trim:  " + result.length());
    	//System.out.println("result:  " + result);
    	//Get rid of the carriage return and newline.
    	return result;
    }
    
    //Simply calls send and then receive for convenience.
    public String sendRead(String message) {
//    	Date date = new Date();
//    	watcher.send(message, date);
//    	try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	String response = watcher.receive(message, date);
//    	return response;
    	send(message);
    	return read();
    }
    
    //How many bytes are waiting to be read.
    public int queueSize() {
    	int size = 0;
    	try {
			size = socket.getInputStream().available();
			//size = in.
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return size;
    }
    
    public void close() {
    	try {
    		//watcher.close();
    		Thread.sleep(3000);
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void test() {
    	try {
			InputStream asdf = socket.getInputStream();
			String a = "";
			int temp = 0;
			int i = 0;
			int[] list = new int[200];
			while (asdf.available() != 0) {
				temp = asdf.read();
				list[i] = temp;
				a += (char) temp;
				i++;
			}
			System.out.println(a);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
}