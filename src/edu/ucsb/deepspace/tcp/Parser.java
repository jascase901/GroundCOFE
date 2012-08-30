package edu.ucsb.deepspace.tcp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author labuser
 * 
 * Takes a script in plain text and maps each word in the to a java function
 *
 */
public class Parser {
	 private static final Map<String, String> cmdMap;
	    static {
	        Map<String, String> aMap = new HashMap<String, String>();
	        aMap.put("move", "stage.gotoPos");
			aMap.put("delay", "stage.pause");
			aMap.put("afterMotion", "!stage.isMoving");
	        cmdMap = Collections.unmodifiableMap(aMap);
	    }

	private String script = "";
	public Parser(String script){
		this.script = parse(script);
			
	}
	public String getScript(){
		return script;
	}
	
	
	private String parse(String script){
		String str="";
		for (char c: script.toCharArray()){
			if (c!=' ')
				str+=c;
		}
		return str;	
	}

	
	public String getParsed(){
		String parsed = getScript();
		for (String str: cmdMap.keySet()){
			parsed = parsed.replaceAll(str, cmdMap.get(str));
		}
		return parsed;
		
	}
	

	

}
