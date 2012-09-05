package edu.ucsb.deepspace.tcp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.ucsb.deepspace.Axis;
import edu.ucsb.deepspace.Coordinate;
import edu.ucsb.deepspace.MoveCommand;
import edu.ucsb.deepspace.ScanCommand;
import edu.ucsb.deepspace.MoveCommand.MoveType;
import edu.ucsb.deepspace.Stage;
import edu.ucsb.deepspace.StageInterface;
import edu.ucsb.deepspace.MoveCommand.MoveMode;
public class Interpereter {
	private String script;
	private StageInterface stage;
	private static final Map<String, Integer> argMap;
	private String debugInfo;
	static {
		Map<String, Integer> aMap = new HashMap<String, Integer>();
		aMap.put("time", 0);
		aMap.put("quit", 0);
		aMap.put("spin", 0);
		aMap.put("stop", 0);
		aMap.put("tellposition", 0);
		aMap.put("delay", 1);
		aMap.put("send", 1);
		aMap.put("abs", 2);
		aMap.put("rel", 2);
		aMap.put("azscan", 4);
		aMap.put("elscan", 4);
		aMap.put("snake", 6);
		aMap.put("square", 6);

		argMap = Collections.unmodifiableMap(aMap);
	}

	public Interpereter(String script, StageInterface stage){
		this.script = script;
		this.stage = stage;
		this.debugInfo = "Nothing got interperted";


	}

	public String Interperet(){
		String str = "";
		String[] methodArray = script.split("\\(");
		int args;
		if(!argMap.containsKey(methodArray[0])){
			System.out.println("Error");
			args = -1;
		}
		else {
			args = argMap.get(methodArray[0]);
		}
		switch (args) {
		case 0:
			str = InterperetZero(methodArray[0]);
			break;
		case 1:
			str = InterperetOne(methodArray[0]);
			break;
		case 2:
			str = InterperetTwo(methodArray[0]);
			break;
		case 4:
			str = InterperetFour(methodArray[0]);
			break;
		case 6:
			str = InterperetSix(methodArray[0]);
		default:
			str=" at interpereter FAIL";

		}



		return str;
	}
	public String InterperetZero(String method){
		debugInfo = "Function:" + method + " Args:";
		switch(method){
		case "quit":
			return "quit";
		case "tellposition":
			return stage.getTxtPosInfo();
		case "spin":
			stage.Spin();
			return "spinning";
		case "stop":
			stage.stopScanning();

		default:
			return "Function:" + method + " Args:";
		}

	}
	public String InterperetOne(String method){
		String[] methodArray = script.split("\\(");
		String arg = methodArray[1].replace(")", "");
		arg = arg.replace(";", "");

		debugInfo="Function:" + method + " Args:1=" + arg;
		switch(method){
		case "send":
			stage.sendCommand(arg.toUpperCase());
			break;
		default:
			return "Error in InterperetOne";
		}
		return "";

	}

	public String InterperetTwo(String method){
		String[] methodArray = script.split("\\(");
		String arg = methodArray[1].replace(";", "");
		arg = arg.replace(")", "");
		String[] args = arg.split(",");
		double param1 = Double.parseDouble(args[0]);
		double param2 = Double.parseDouble(args[1]);
		MoveCommand mc;
		this.debugInfo = "Function:" + method + " Args:2=" + arg;

		switch(method){
		case "abs":
			mc = new MoveCommand(MoveMode.ABSOLUTE, MoveType.DEGREE, param1, param2);
			stage.move(mc);
			break;
		case "rel":
			mc = new MoveCommand(MoveMode.RELATIVE, MoveType.DEGREE, param1, param2);
			stage.move(mc);
			break;
		default:
			break;
		}

		return "";
	}

	public String InterperetFour(String method){

		String[] methodArray = script.split("\\(");
		String arg = methodArray[1].replace(";", "");
		arg = arg.replace(")", "");
		String[] args = arg.split(",");
		debugInfo = "Function:" + method + " Args:4=" + arg;

		double param1 = Double.parseDouble(args[0]);
		double param2 = Double.parseDouble(args[1]);
		double param3 = Double.parseDouble(args[2]);
		double param4 = Double.parseDouble(args[3]);
		switch (method){
		case "azscan":
			ScanCommand azSc = new ScanCommand(param1, param2, param3, (int)param4);
			stage.startScanning(azSc, null,false);
			break;
		case "elscan":
			ScanCommand elSc = new ScanCommand(param1, param2, param3, (int)param4);
			stage.startScanning(null, elSc, false);
			break;






		default:
			return "Error Something went wrong in InterperetFour";
		}
		return "";
	}
	public String InterperetSix(String method){
		String[] methodArray = script.split("\\(");
		String arg = methodArray[1].replace(";", "");
		arg = arg.replace(")", "");
		String[] args = arg.split(",");
		debugInfo = "Function:" + method + " Args:4=" + arg;
		double param1 = Double.parseDouble(args[0]);
		double param2 = Double.parseDouble(args[1]);
		double param3 = Double.parseDouble(args[2]);
		double param4 = Double.parseDouble(args[3]);
		double param5 = Double.parseDouble(args[4]);
		double param6 = Double.parseDouble(args[5]);
		ScanCommand azSc = new ScanCommand(param1, param2, param5, (int)param6);
		ScanCommand elSc = new ScanCommand(param3, param4, param5, (int)param6);
		switch(method){
		case("square"):
			stage.startScanning(azSc, elSc, true);
			break;
		case("snake"):
			stage.startScanning(azSc, elSc, false);


		}
		
		return "";


	}
	public String getDebug(){
		return debugInfo;
	}




}
