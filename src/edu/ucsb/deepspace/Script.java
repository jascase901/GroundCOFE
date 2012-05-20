package edu.ucsb.deepspace;

public class Script {
	
	private String out;
	private int index;
	private String scriptName;
	
	public Script(String scriptName, int index) {
		this.scriptName = scriptName;
		this.out = "DL " + index + "\r" + scriptName + "\r";
		this.index = index;
	}
	
	public void add(String str) {
		index++;
		out = out + str + "\r";
	}
	
	public String getScript() {
		return out + "\\\r";
	}
	
	public int size() {
		return index;
	}
	
	String getScriptName() {
		return scriptName;
	}

}