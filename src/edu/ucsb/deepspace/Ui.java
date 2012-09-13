package edu.ucsb.deepspace;

import java.util.Set;

public interface Ui {

	boolean debug = false;

	void setMinMaxAzEl(double minAz, double maxAz, double minEl, double maxEl);

	void setVelAccAzEl(double maxVelAz, double maxAccAz, double maxVelEl,
			double maxAccEl);

	void setMaxMoveRel(double maxMoveRelAz, double maxMoveRelEl);

	void updateScriptArea(String string, Set<String> findLoaded);

	void raDecTrackingButtonUpdater(boolean b, boolean c);

	void updateTxtAzElRaDec(String out);

	String updateStatusArea(String string);

	void updateReps(double reps);

	void enableScanButtons();

	void controlMoveButtons(boolean b);

	void setRaDec(double ra, double dec);

	void updateBaseBalloonLoc();

	void updateMotorButton(boolean motorState, Axis az);

	void buttonEnabler(String name);

	String updateTxtPosInfo(String info);

	void setGoalPos(String format, Axis axis);

	void alive();

	
}
