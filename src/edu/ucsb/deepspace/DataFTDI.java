package edu.ucsb.deepspace;

public class DataFTDI implements DataInterface {
	
	private ActStatus az, el;
	
	public DataFTDI() {
		
	}
	
	@Override
	public String info() {
		String out = "Actuator Information\n";
    	out += "AzGoal " + az.getStepperGoal() + "\n";
    	out += "AzNow " + az.getStepperNow() + "\n";
    	out += "AzEncPosition " + az.getEncoderPosition() + "\n";
    	//out += "AzEncoderInd " + stage.getAzEncInd() + "\n";
    	out += "stepperAtInd " + az.getStepperAtIndex() + "\n\n";
    	out += "ElGoal " + el.getStepperGoal() + "\n";
    	out += "ElNow " + el.getStepperNow() + "\n";
    	out += "ElEncPosition " + el.getEncoderPosition() + "\n";
    	out += "ElEncoderInd " + el.getEncoderIndex() + "\n";
    	out += "stepperAtIndex " + el.getStepperAtIndex() + "\n";
    	//out += "Velocity " + velocity + "\n";
    	//out += "\nAz index is at " + azIndexDeg + " degrees.";
    	//out += "\nEl index is at " + elIndexDeg + " degrees.";
    	return out;
	}

	@Override
	public boolean moving() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double azPos() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double elPos() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	class ActStatus {
		private final int stepperNow, stepperGoal, encoderPosition, encoderAtIndex, stepperAtIndex;
		
		public ActStatus(int stepperNow, int stepperGoal, int encoderPosition, int encoderIndex, int stepperAtIndex) {
			this.stepperNow = stepperNow;
			this.stepperGoal = stepperGoal;
			this.encoderPosition = encoderPosition;
			this.encoderAtIndex = encoderIndex;
			this.stepperAtIndex = stepperAtIndex;
		}

		public int getStepperNow() {
			return stepperNow;
		}

		public int getStepperGoal() {
			return stepperGoal;
		}

		public int getEncoderPosition() {
			return encoderPosition;
		}

		public int getEncoderIndex() {
			return encoderAtIndex;
		}

		public int getStepperAtIndex() {
			return stepperAtIndex;
		}
		
		public boolean allZero() {
			boolean one = stepperNow == 0;
			boolean two = stepperGoal == 0;
			boolean three = encoderPosition == 0;
			boolean four = encoderAtIndex == 0;
			boolean five = stepperAtIndex == 0;
			return one && two && three && four && five;
		}
	}

}
