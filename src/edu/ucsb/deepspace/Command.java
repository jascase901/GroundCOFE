package edu.ucsb.deepspace;

import java.util.Date;

public class Command {
	
	private String command;
	private String response;
	private Date sendDate;
	
	public Command(String command, Date sendDate) {
		this.command = command;
		this.sendDate = sendDate;
	}
	
	public void updateResponse(String response) {
		this.response = response;
	}
	
	public String getCommand() {return this.command;}
	public String getResponse() {return this.response;}
	public Date getSendDate() {return this.sendDate;}
	
	public boolean validate(String command, Date date) {
		System.out.println("command's command: " + this.command);
		System.out.println("command's date: " + this.sendDate.getTime());
		System.out.println("command: " + command);
		System.out.println("date: " + date.getTime());
		boolean a = this.command.equals(command);
		boolean b = this.sendDate.equals(date);
		return a&&b;
	}

}