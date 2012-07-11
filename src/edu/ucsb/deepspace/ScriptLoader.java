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
	private Script fraster;
	private Script azScan;
	private Script elScan;
	
	
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
		scripts.put("FRASTER", fraster);
		scripts.put("#AZSCAN", azScan);
		scripts.put("#ELSCAN", elScan);
		
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
		fraster();
		azScan();
		elScan();
		
		protocol.sendRead(homeA.getScript());
		pause();
		protocol.sendRead(homeB.getScript());
		pause();
		protocol.sendRead(readerInfo.getScript());
		pause();
		protocol.sendRead(raster.getScript());
		pause();
		protocol.sendRead(fraster.getScript());
		pause();
		protocol.sendRead(azScan.getScript());
		pause();
		protocol.sendRead(elScan.getScript());
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
		
		raster = new Script("#RASTER", size);
		
		//raster.add("time = 10");
		//raster.add("minAz = 0");
		//raster.add("maxAz =  10000");
		//raster.add("minEl = 0");
		//raster.add("maxEl = 100");
		//Amount of az and el scans for this one it is actually 20 for az because each az is done twice
		raster.add("lineNum=10");
		//distance moved
		raster.add("dx = maxAz - minAz");
		raster.add("dy = (maxEl - minEl)/lineNum");
		raster.add("vx = (dx/(time*.2))*2*lineNum");
		raster.add(	"vy = (dy/(time*.8))*lineNum");
		raster.add("y=minEl");
		raster.add("SP vx, vy");
		raster.add("#REPEAT");
		raster.add("y = dy + y");
		raster.add("PA ,y");
		raster.add("BG");
		raster.add("AM");
		raster.add("PA minAz");
		raster.add("BG");
		raster.add("AM");
		raster.add("WT 200");
		raster.add("PA maxAz");
		raster.add("BG");
		raster.add("AM");
		raster.add("WT 200");
		raster.add("JP #REPEAT, y<maxEl");
		raster.add("EN");
		size += raster.size();

	
	}
	
	public void fraster(){
		fraster = new Script("#FRASTER", size);

		fraster.add("time = 20");
		fraster.add("minAz = 0");
		fraster.add("maxAz = 17066");
		fraster.add("minEl = 0");
		fraster.add("maxEl = 66.6");

		fraster.add("lineNum=10");
		fraster.add("dx = maxAz - minAz");
		fraster.add("dy = (maxEl - minEl)/lineNum");

		fraster.add("vx = (dx/(time*.5))*lineNum");
		fraster.add("vy = (dy/(time*.5))*lineNum ");

		fraster.add("alt = 1");
		fraster.add("y=minEl");


		fraster.add("SP vx, vy");
		fraster.add("#SNAKE");

		fraster.add("alt = alt*-1");
		fraster.add("y = dy + y");

		fraster.add("x = minAz");
		fraster.add("IF(alt<0)");
		fraster.add("x = maxAz");
		fraster.add("ENDIF");
		fraster.add("PA x");
		fraster.add("BG");
		fraster.add("AM");
		fraster.add("WT 500");
		fraster.add("PA x,y");
		fraster.add("BG");
		fraster.add("AM");
		fraster.add("WT 500");
		fraster.add("JP #SNAKE, y<maxEl");
		fraster.add("EN");
		size += fraster.size();


	}
	private void azScan() {
		azScan = new Script("#AZSCAN", size);
		//counter
		azScan.add("n=0");
		//v3 = speed
		azScan.add("SP 10000");
		azScan.add("PA V7,");
		azScan.add("SP V3");
		azScan.add("#LOOP3");
		//v7=min az
		azScan.add("PA V7,");
		azScan.add("BG");
		azScan.add("AM");
		azScan.add("WT 200");
		//v1 = max az
		azScan.add("PA V1,");
		azScan.add("BG");
		azScan.add("AM");
		azScan.add("WT 200");
		azScan.add("n = n+1");
		azScan.add("JP #LOOP3, n<V6 ");
		azScan.add("EN");
		size += azScan.size();
		
		
		
	}
	private void elScan() {
		elScan = new Script("#ELSCAN", size);
		//counter
		elScan.add("SP ,1000");
		elScan.add("PA ,V7");
		
		elScan.add("n=0");
		//v3 = speed
		elScan.add("SP ,V3");
		elScan.add("#LOOP4");
		//v7=min az
		elScan.add("PA ,V7");
		elScan.add("BG");
		elScan.add("AM");
		elScan.add("WT 200");
		//v1 = max az
		elScan.add("PA ,V1");
		elScan.add("BG");
		elScan.add("AM");
		elScan.add("WT 200");
		elScan.add("n = n+1");
		elScan.add("JP #LOOP4, n<V6 ");
		elScan.add("EN");
		size += elScan.size();
		
		
		
	}
	
	
	private void readerInfo() {
		readerInfo = new Script("#READERI", size);
		//There is a maximum amount of data, that can be sent in one line, so if we want to add more we need another solution
		String temp = "MG _TPA,_TVA,_JGA,_ACA,_TPB,_TVB,_JGB,_ACB,_MOA,_MOB,_BGA,_BGB,_HX0,_HX1,_HX2";
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