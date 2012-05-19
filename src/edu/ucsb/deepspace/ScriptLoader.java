package edu.ucsb.deepspace;

public class ScriptLoader {
	
	private CommGalil protocol;
	
	private Script homeA;
	private Script homeB;
	
	public ScriptLoader() {
		protocol = new CommGalil(23);
	}
	
	public void load() {
		System.out.println(protocol.sendRead("LL"));
		
		homeA = new Script("#HOMEAZ", 0);
		indexAz();
		
		homeB = new Script("#HOMEB", homeA.size()+20);
		indexEl();
		
		System.out.println(homeA.getScript());
		System.out.println(homeB.getScript());
		
		protocol.send(homeA.getScript());
		pause();
		protocol.send(homeB.getScript());
		pause();
		protocol.close();
	}
	
	public void close() {
		protocol.close();
	}
	
	private void indexAz() {
		
		System.out.println("AZ");
		  //NO The galil manual is incorrect in some of the code specifications.
		  //NO for one, it states that you must strip leading spaces from programs
		  //NO this is incorrect.

		  //NO Empty lines are a problem, though. The galil software automtically sticks
		  //NO a comment symbol in them (" ' ")
		  //NO This is easy enough to do myself.

		  //NO Apparently any line cannot be more then 80 characters long, though

		homeA.add("IF (_MOA)");
		    //NO "BG" commands fail if the motor is off. Therefore, check motor state
		homeA.add("MG \"Motor is off. Cannot execute home operation\"");

		homeA.add("ELSE");
		    //NO Save acceleration and jog speed values
		homeA.add("T1 = _JGA");
		homeA.add("T2 = _ACA");

		    //NO then overwrite them
		homeA.add("MG \"Homing\", T1");
		homeA.add("JGA=150000");
		homeA.add("ACA=50000");

		    //NO "FE" - find the opto-edge
		homeA.add("FE A");
		homeA.add("BG A");
		homeA.add("AM A");
		homeA.add("MG \"Found Opto-Index\"; TP");

		    //NO Turn the jog speed WAAAY down when searching for the index
		homeA.add("JGA=500");

		    //NO Do the index search ("FI")
		homeA.add("FI A");
		homeA.add("BG A");

		homeA.add("AM A");
		homeA.add("MG \"Motion Done\"; TP");

		    //NO Finally, restore accel and jog speeds from before routine was run
		homeA.add("JGA=T1");
		homeA.add("ACA=T2");

		homeA.add("ENDIF");
		homeA.add("EN");
		
		System.out.println("asdfadsfadfs");
	}
	
	private void indexEl() {
		System.out.println("indexEl scriptuploader");
		String axisName = "B";
		
		//homeB.add("#HOMEB");
		
		homeB.add("T1 = _JG" + axisName);
		homeB.add("T2 = _AC" + axisName);
		
		double jg = 1000d;
		homeB.add("JG" + axisName + "=" + jg);
		
		// Do the index search ("FI")
		homeB.add("FI" + axisName);
		homeB.add("BG" + axisName);
		homeB.add("AM" + axisName);
		homeB.add("PRB=3900");
		homeB.add("BG" + axisName);
		jg = 50d;
		homeB.add("AM" + axisName);
		homeB.add("JG" + axisName + "=" + jg);
		homeB.add("FI" + axisName);
		homeB.add("BG" + axisName);
		homeB.add("TPB");
		homeB.add("EN");
		//pause();
		//homeB.add("\\.");
	}
	
	private void pause() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}