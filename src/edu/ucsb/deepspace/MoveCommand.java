package edu.ucsb.deepspace;

/**
 * Small data aggregate class that consolidates everything needed for one axis to move.
 * @author Reed
 */
public class MoveCommand {
	
	/**
	 * Represents relative or absolute motion.
	 */
	public static enum MoveMode {
		RELATIVE, ABSOLUTE;
	}
	
	/**
	 * Represents the units of amount.  Either encoder pulses or degrees.
	 */
	public static enum MoveType {
		ENCODER, DEGREE;
	}
	
	private MoveMode mode;
	private MoveType type;
	private Axis axis;
	private double amount;
	
	/**
	 * 
	 * @param mode relative or absolute
	 * @param type encoder or degree
	 * @param axis az or el
	 * @param amount of motion to move
	 */
	public MoveCommand(MoveMode mode, MoveType type, Axis axis, double amount) {
		this.mode = mode;
		this.type = type;
		this.axis = axis;
		this.amount = amount;
	}
	
	public MoveMode getMode(){ return mode;}
	public MoveType getType() { return type;}
	public Axis getAxis() {return axis;}
	public double getAmount() {return amount;}
}
