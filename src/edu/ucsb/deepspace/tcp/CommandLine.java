package edu.ucsb.deepspace.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Formatter;
import java.util.Locale;
import java.util.Set;

import edu.ucsb.deepspace.Axis;
import edu.ucsb.deepspace.ScanCommand;
import edu.ucsb.deepspace.Stage;
import edu.ucsb.deepspace.StageInterface;
import edu.ucsb.deepspace.Ui;

public class CommandLine implements Ui{
	private boolean debug = false;
	private StageInterface stage;
	private Stage.StageTypes stageTypes;
	private Parser parser;
	private Interpereter interpereter;
	private boolean tellposition = false;
	public CommandLine(StageInterface stage, Stage.StageTypes stageType){
		this.stage = stage;
		this.stageTypes = stageTypes;

		
		
	}
	public void alive(){
		try {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String command = "";
		while(!command.equals("quit") ){
		 System.out.print("> ");
	       
	       
	       
	         command = br.readLine();
	         parser = new Parser(command);
	         command = parser.getParsed();
	         interpereter = new Interpereter(command, stage);
	         command = interpereter.Interperet();
	         System.out.println(command);
	         
		}
	       } catch (IOException e) {
	         System.out.println("Error!");
	         System.exit(1);
	       }
	       

		}
	
	public void setTellPosition(boolean value){
		tellposition = value;
	}
	public boolean tellPosition(){
		return tellposition;
	}
	
	public void setMinMaxAzEl(double minAz, double maxAz, double minEl, double maxEl){
		
	}

	public void setVelAccAzEl(double maxVelAz, double maxAccAz, double maxVelEl,
			double maxAccEl){
		
	}

	public void setMaxMoveRel(double maxMoveRelAz, double maxMoveRelEl){}

	public void updateScriptArea(String string, Set<String> findLoaded){}

	public void raDecTrackingButtonUpdater(boolean b, boolean c){}

	public void updateTxtAzElRaDec(String out){}

	public String updateStatusArea(String string){
		   return "";
	}
	

	public void updateReps(double reps){}

	public void enableScanButtons(){}

	public void controlMoveButtons(boolean b){}

	public void setRaDec(double ra, double dec){}

	public void updateBaseBalloonLoc(){}

	public void updateMotorButton(boolean motorState, Axis az){}

	public void buttonEnabler(String name){}

	public String updateTxtPosInfo(String info){
		return "";
	}

	public void setGoalPos(String format, Axis axis){}

	

}
