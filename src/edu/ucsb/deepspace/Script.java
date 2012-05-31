package edu.ucsb.deepspace;

public class Script {
	
	private String out;
	//private int index;
	private String scriptName;
	private int length;
	
	/**
	 * Instantiates a the beginning of a script with its name being scriptName, 
	 * and its starting position being index
	 */
	public Script(String scriptName, int index) {
		this.scriptName = scriptName;
		this.out = "DL " + index + "\r" + scriptName + "\r";
		//this.index = index;
	}
	
	/**
	 * 
	 * @param str the command you want to enter
	 * appends the command to your script
	 */
	public void add(String str) {
		length++;
		out = out + str + "\r";
	}
	/**
	 * 
	 * @return the intro and body of your script as well as an ending character
	 */
	public String getScript() {
		return out + "\\\r";
	}
	
	public int size() {
		return length + 1;
	}
	
	public int length() {
		return length;
	}
	
//	String getScriptName() {
//		return scriptName;
//	}

}