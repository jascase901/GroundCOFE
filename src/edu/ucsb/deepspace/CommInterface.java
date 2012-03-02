package edu.ucsb.deepspace;

public interface CommInterface {
	
	public void close();
	
	public String read();
	
	public int queueSize();

}
