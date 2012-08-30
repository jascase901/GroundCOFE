package edu.ucsb.deepspace.tcp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Interpereter {
	private String script;
	private static final Map<String, Integer> argMap;
    static {
        Map<String, Integer> aMap = new HashMap<String, Integer>();
        aMap.put("time", 0);
        aMap.put("tellposition", 0);
        aMap.put("delay", 1);
        aMap.put("move", 2);
        argMap = Collections.unmodifiableMap(aMap);
    }

	public Interpereter(String script){
		this.script = parse(script);
		

	}

	private String parse(String script){
		String str="";
		for (char c: script.toCharArray()){
			if (c!=' ')
				str+=c;
		}
		return str.toLowerCase();	
		
	}
	public String Interperet(){
		String str = "";
		String[] methodArray = script.split("\\(");
		int args = argMap.get(methodArray[0]);
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
			default:
				str=" at interpereter FAIL";
				
		}
		
		return str;
	}
	public String InterperetZero(String method){
		return "Function:" + method + " Args:";
	}
	public String InterperetOne(String method){
		String[] methodArray = script.split("\\(");
		String arg = methodArray[1].replace(");", "");
		return "Function:" + method + " Args:1=" + arg;
	}
	
	public String InterperetTwo(String method){
		String[] methodArray = script.split("\\(");
		String arg = methodArray[1].replace(");", "");
		arg = methodArray[1].replace(")", "");
		System.out.println("Executing");
		return "Function:" + method + " Args:2=" + arg;
		
	}




}
