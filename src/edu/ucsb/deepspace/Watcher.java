package edu.ucsb.deepspace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Watcher {
	
	private BlockingQueue<String> newCommands = new ArrayBlockingQueue<String>(50);
	private BlockingQueue<String> writtenCommands = new ArrayBlockingQueue<String>(50);
	private BlockingQueue<Date> newDates = new ArrayBlockingQueue<Date>(50);
	private BlockingQueue<Date> writtenDates = new ArrayBlockingQueue<Date>(50);
	private BlockingQueue<String> responses = new ArrayBlockingQueue<String>(50);
	private BlockingQueue<Command> aaa = new ArrayBlockingQueue<Command>(50);
	
	PrintWriter out;
	BufferedReader in;
	private Timer reader = new Timer("reader", true);
	private Timer writer = new Timer("writer", true);
	private Timer maker = new Timer("maker", true);
	
	public Watcher(PrintWriter out, BufferedReader in) {
		this.out = out;
		this.in = in;
		reader.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				read();
			}
		}, 0, 100);
		writer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				String command;
				Date date;
				try {
					command = newCommands.take();
					date = newDates.take();
					write(command);
					writtenCommands.put(command);
					writtenDates.put(date);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, 0, 100);
		maker.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					String command = writtenCommands.take();
					Date date = writtenDates.take();
					String response = responses.take();
					Command c = new Command(command, date);
					c.updateResponse(response);
					aaa.add(c);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, 0, 100);
	}
	
	public void send(String command, Date date) {
		try {
			newCommands.put(command);
			newDates.put(date);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public String receive(String command, Date date) {
		String response = "asdf";
		for (Command c : aaa) {
			//System.out.println("hello2222");
			//c.validate(command, date);
			if (c.validate(command, date)) {
				System.out.println("hello");
				response = c.getResponse();
				System.out.println("response:  " + response);
			}
			aaa.remove(c);
			break;
		}
		return response;
	}
	
	private void read() {
		String result = "nothing";
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
    	
//    	//Deal with error message, if necessary.
//    	if (result.contains("?")) {
//    		System.out.println("Syntax Error");
//    		System.out.println("Returned value: " + result);
//    		System.out.println("Error code: " + sendRead("TC1"));
//    	}
    	
    	//System.out.println("length before trim:  " + result.length());
    	result = result.replace("\r\n", "");
    	//System.out.println("length after trim:  " + result.length());
    	//System.out.println("result:  " + result);
    	//Get rid of the carriage return and newline.
    	responses.add(result);
	}
	
	private void write(String command) {
		out.println(command);
	}
	
	

}