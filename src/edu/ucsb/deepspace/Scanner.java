package edu.ucsb.deepspace;

public class Scanner {
	
	private Stage stage;
	private ActInterface az, el;
	
	public Scanner(Stage stage, ActInterface az, ActInterface el) {
		this.stage = stage;
		this.az = az;
		this.el = el;
	}
	
	public void singleAxisScan() {
		
	}
	
	public void rasterScan() {
		
	}
	
}