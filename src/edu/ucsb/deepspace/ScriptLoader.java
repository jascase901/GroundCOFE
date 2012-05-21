package edu.ucsb.deepspace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScriptLoader {
	
	private CommGalil protocol;
	
	private Script homeA;
	private Script homeB;
	private Script readerInfo;
	private Set<String> loadedScriptNames;
	private Map<String, Script> scripts;
	
	public ScriptLoader() {
		protocol = new CommGalil(23);
		loadedScriptNames = new HashSet<String>();
		scripts = new HashMap<String, Script>();
		scripts.put("#HOMEAZ", homeA);
		scripts.put("#HOMEB", homeB);
		scripts.put("#READERI", readerInfo);
	}
	
	public void check() {
		String labels = protocol.sendRead("LL");
		String[] split = labels.split("\r\n");
		for (String s : split) {
			String name = s.split("=")[0];
			loadedScriptNames.add(name);
		}
	}
	
	public void load() {
		Set<String> scriptsToLoad = scripts.keySet();
		
		if (loadedScriptNames.containsAll(scriptsToLoad)) {
			return;
		}
		
		indexAz();
		indexEl();
		readerInfo();
		
		protocol.send(homeA.getScript());
		pause();
		protocol.send(homeB.getScript());
		pause();
		protocol.send(readerInfo.getScript());
		pause();
	}
	
	public void close() {
		protocol.close();
	}
	
	private void indexAz() {
		homeA = new Script("#HOMEAZ", 0);
		homeA.add("IF (_MOA)");
		//"BG" commands fail if the motor is off. Therefore, check motor state
		homeA.add("MG \"Motor is off. Cannot execute home operation\"");

		homeA.add("ELSE");
		//Save acceleration and jog speed values
		homeA.add("T1 = _JGA");
		homeA.add("T2 = _ACA");

		//then overwrite them
		homeA.add("MG \"Homing\", T1");
		homeA.add("JGA=150000");
		homeA.add("ACA=50000");

		//"FE" - find the opto-edge
		homeA.add("FE A");
		homeA.add("BG A");
		homeA.add("AM A");
		homeA.add("MG \"Found Opto-Index\"; TP");

		//Turn the jog speed WAAAY down when searching for the index
		homeA.add("JGA=500");

		//Do the index search ("FI")
		homeA.add("FI A");
		homeA.add("BG A");

		homeA.add("AM A");
		homeA.add("MG \"Motion Done\"; TP");

		//Finally, restore accel and jog speeds from before routine was run
		homeA.add("JGA=T1");
		homeA.add("ACA=T2");

		homeA.add("ENDIF");
		homeA.add("EN");
	}
	
	private void indexEl() {
		homeB = new Script("#HOMEB", homeA.size()+20);
		String axisAbbrev = Axis.EL.getAbbrev();
		
		homeB.add("T1 = _JG" + axisAbbrev);
		homeB.add("T2 = _AC" + axisAbbrev);
		
		double jg = 1000d;
		homeB.add("JG" + axisAbbrev + "=" + jg);
		
		// Do the index search ("FI")
		homeB.add("FI" + axisAbbrev);
		homeB.add("BG" + axisAbbrev);
		homeB.add("AM" + axisAbbrev);
		homeB.add("PRB=3900");
		homeB.add("BG" + axisAbbrev);
		jg = 50d;
		homeB.add("AM" + axisAbbrev);
		homeB.add("JG" + axisAbbrev + "=" + jg);
		homeB.add("FI" + axisAbbrev);
		homeB.add("BG" + axisAbbrev);
		homeB.add("EN");
	}
	
	private void readerInfo() {
		readerInfo = new Script("#READERI", homeB.size()+20);
		String temp = "MG _TPA, _TVA, _JGA, _ACA, _TPB, _TVB, _JGB, _ACB, _MOA, _MOB, _BGA, _BGB";
		readerInfo.add(temp);
		readerInfo.add("EN");
	}
	
	private void pause() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}