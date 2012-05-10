package edu.ucsb.deepspace;

/**
 * Small data aggregate class that consolidates everything needed for one axis to move.
 * @author Reed
 *
 */
public class MoveCommand {
	
	/**
	 * Represents relative or absolute motion.
	 * @author Reed
	 *
	 */
	public static enum MoveMode {
		RELATIVE, ABSOLUTE;
	}
	
	/**
	 * Represents the units of amount.  Either encoder pulses or degrees.
	 * @author Reed
	 *
	 */
	public static enum MoveType {
		ENCODER, DEGREE;
	}
	
	private MoveMode mode;
	private MoveType type;
	private ActInterface.axisType axis;
	private double amount;
	
	/**
	 * 
	 * @param mode relative or absolute
	 * @param type encoder or degree
	 * @param axis az or el
	 * @param amount of motion to move
	 */
	public MoveCommand(MoveMode mode, MoveType type, ActInterface.axisType axis, double amount) {
		this.mode = mode;
		this.type = type;
		this.axis = axis;
		this.amount = amount;
	}
	
	public MoveMode getMode(){ return mode;}
	public MoveType getType() { return type;}
	public ActInterface.axisType getAxis() {return axis;}
	public double getAmount() {return amount;}
}
