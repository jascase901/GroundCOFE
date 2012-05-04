package edu.ucsb.deepspace;

public class ReaderGalil extends Thread implements ReaderInterface {
	
	private boolean flag = true;
	private CommGalil protocol;
	private final Stage stage;
	private String azAxis = "";
	@SuppressWarnings("unused")
	private String elAxis = "";
	
	public ReaderGalil(Stage stage) {
		this.setDaemon(true);
		//protocol = CommGalil.getInstance();
		
		this.stage = stage;
		
		//TODO methods that fetch these from stage
		//for now just default them
		//azAxis = stage.getAzAxis();
		//elAxis = stage.getElAxis();
		azAxis = "A";
		elAxis = "B";
	}
	
	private String tellPos = "TP";
	private String tellVel = "TV";
	DataGalil data;
	private boolean pauseFlag = false;
	
	public void togglePauseFlag() {
		System.out.println("hi");
		this.pauseFlag = !pauseFlag;
	}
	
	public void run() {
		protocol = new CommGalil(13358);
		while (flag) {
			if (!pauseFlag) {
				String azPos = protocol.sendRead(tellPos + azAxis);
				String azVel = protocol.sendRead(tellVel + azAxis);
				String elPos = protocol.sendRead(tellPos + elAxis);
				String elVel = protocol.sendRead(tellVel + elAxis);
				/*System.out.println("elPos: " + elPos);
				System.out.println("elVel: " + elVel);*/
				//String azPos = "4";
				//String azVel = "5";
				data = new DataGalil();
				data.makeAz(azPos, azVel);
				data.makeEl(elPos, elVel);
				stage.updatePosition(data);
	
	
				//String elPos = protocol.sendRead(tellPos + elAxis);
			}
			pause(1000);
		}
		protocol.close();
	}
	
	public void stop2() {
		flag = false;
	}
	
	private void pause(int msToPause) {
		try {
			Thread.sleep(msToPause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}