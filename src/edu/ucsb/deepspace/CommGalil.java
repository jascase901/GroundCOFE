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
/**
 * Lowest level methods that communicate with Galil.
 *
 *
 */
public class CommGalil implements CommInterface {
	private PrintWriter out;
	private BufferedReader in;
	private Socket socket = null;
	boolean connection = false;
	private String previousCommand = "";
	
	int port = 0;
//	private static int readCount = 0;
//	private static int sendCount = 0;
//	private static List<String> send = new ArrayList<String>();
//	private static List<String> response = new ArrayList<String>();
//	private static List<String> threads = new ArrayList<String>();
	
	/**
	 * Creates a connection with Galil and instantiates a CommGalil object.
	 * @param port to connect to
	 */
	public CommGalil(int port) {
		System.out.println("comm galil constructor" + port);
		this.port = port;
		try {
			socket = new Socket();

			socket.connect(new InetSocketAddress("192.168.1.241", port), 1000);
		
			socket.setSoTimeout(500);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			connection = true;
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
	/**
	 * Sends a message to Galil which is how the program communicates with it.
	 * @param message to send
	 */
    public void send(String message) {
    	//sendCount++;
    	previousCommand = message;
    	out.println(message);
    	//System.out.print(message);
    }
    /**
     * Reads output from Galil.
     */
    public String read() {
    	//readCount++;
    	//Thread tr = Thread.currentThread();
    	//threads.add(tr.getName());
    	
    	//System.out.println(readCount + "thread name:  " + tr.getName());
    	//System.out.println("port:  " + port);
    	
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
    	
    	if (result.equalsIgnoreCase("?")) {
    		System.out.println("previousCommand: " + previousCommand);
    		System.out.println(port + " result: " + result);
    		System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    	}
    	//Deal with error message, if necessary.
    	if (result.contains("?")) {
    		System.out.println("Syntax Error");
    		System.out.println("Command Sent: " + previousCommand);
    		System.out.println("Returned value: " + result);
    		System.out.println("Error code: " + sendRead("TC1"));
    	}
    	if (result.equalsIgnoreCase("?")) {
    		System.out.println("This should never ever happen.");
    		result = "0";
    	}
    	//response.add(result);
    	//System.out.println("length before trim:  " + result.length());
    	result = result.replace("\r\n:", "");
    	//System.out.println("length after trim:  " + result.length());
    	//System.out.println("result:  " + result);
    	//Get rid of the carriage return and newline.
    	
    	return result;
    }
    
    //Simply calls send and then receive for convenience.
    /**
     * Sends a message to Galil and receives a reply.
     * @param message to send
     * @return message from Galil
     */
    public String sendRead(String message) {
    	send(message);
    	String temp = read();
    	
    	return temp;
    }
    
    //How many bytes are waiting to be read.
    /**
     * Gets the amount of bytes waiting to be read from Galil.
     */
    public int queueSize() {
    	int size = 0;
    	try {
			size = socket.getInputStream().available();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return size;
    }
    /**
     * Closes the connection with Galil.
     */
    public void close() {

    	try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void initialize() {
    	sendRead("CF I;CW 2"); //causes responses to be sent over the port that sent this command
    	read();
    	
    }
    
    /**
     * Reads off what Galil is wanting to send.
     */
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
			e.printStackTrace();
		}
    }
	
}