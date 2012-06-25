package edu.ucsb.deepspace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScriptLoader {
	
	private CommGalil protocol;
	
	private Script homeA;
	private Script homeB;
	private Script raster;
	
	private Script readerInfo;
	private Set<String> loadedScriptNames;
	private Map<String, Script> scripts;
	private int size = 0;
	
	public ScriptLoader() {
		protocol = new CommGalil(23);
		loadedScriptNames = new HashSet<String>();
		scripts = new HashMap<String, Script>();
		scripts.put("#HOMEAZ", homeA);
		scripts.put("#HOMEB", homeB);
		scripts.put("#READERI", readerInfo);
		scripts.put("#RASTER", raster);
	}
	
	public Set<String> findExpected() {
		System.out.println(scripts.keySet());
		return scripts.keySet();
	}
	
	public Set<String> findLoaded() {
		protocol.read();
		protocol.read();
		protocol.read();
		String labels = protocol.sendRead("LL");
		System.out.println(labels);
		String[] split = labels.split("\r\n");
		for (String s : split) {
			String name = s.split("=")[0];
			loadedScriptNames.add(name);
		}
		System.out.println(loadedScriptNames);
		return loadedScriptNames;
	}
	
	public boolean readerReady() {
		return loadedScriptNames.contains("#READERI");
	}
	
//	public void check() {
//		String labels = protocol.sendRead("LL");
//		String[] split = labels.split("\r\n");
//		for (String s : split) {
//			String name = s.split("=")[0];
//			loadedScriptNames.add(name);
//		}
//	}
	
	public void load() {
		Set<String> scriptsToLoad = scripts.keySet();
		
		if (loadedScriptNames.containsAll(scriptsToLoad)) {
			return;
		}
		
		indexAz();
		indexEl();
		readerInfo();
		raster();
		
		protocol.sendRead(homeA.getScript());
		pause();
		protocol.sendRead(homeB.getScript());
		pause();
		protocol.sendRead(readerInfo.getScript());
		pause();
		protocol.send(raster.getScript());
		pause();
	}
	
	public void close() {
		protocol.close();
	}
	
	private void indexAz() {
		homeA = new Script("#HOMEAZ", size);
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
		size = homeA.size();
	}
	
	private void indexEl() {
		homeB = new Script("#HOMEB", size);
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
		homeB.add("JG" + axisAbbrev + "=T1");
		homeB.add("AC" + axisAbbrev + "=T2");
		homeB.add("EN");
		size += homeB.size();
	}
	
	public void raster() {
		raster = new Script("#RASTER", readerInfo.size()+20);
		raster.add("n=0");
		raster.add("j=0");
		raster.add("i=0");
		
		
	

		raster.add ("SP V3,V4");
		raster.add("#LOOP2");
		raster.add("#LOOP");
		raster.add("j=n*-V2");
		raster.add("WT 200");
		raster.add("PA V7,j");
		raster.add(" BG");
		raster.add("AM");
		raster.add("WT 200");
		raster.add("PA V1,j");
		raster.add("BG");
		raster.add("AM");
		raster.add("n=n+1");
		raster.add("JP #LOOP, n<V5");
		raster.add("j=0");
		raster.add("n=0");
		raster.add("i=i+1");
		raster.add("MG i");
		raster.add("JP #LOOP2, i<V6");
		raster.add("EN");

		
		
		
	
	}
	
	private void readerInfo() {
		readerInfo = new Script("#READERI", size);
		String temp = "MG _TPA, _TVA, _JGA, _ACA, _TPB, _TVB, _JGB, _ACB, _MOA, _MOB, _BGA, _BGB";
		readerInfo.add(temp);
		readerInfo.add("EN");
		size += readerInfo.size();
	}
	
	private void pause() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}