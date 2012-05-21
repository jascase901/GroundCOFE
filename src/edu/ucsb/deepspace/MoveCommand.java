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
	private Double azAmount;
	private Double elAmount;
	
	/**
	 * 
	 * @param mode relative or absolute
	 * @param type encoder or degree
	 * @param axis az or el
	 * @param azAmount of motion to move
	 */
	public MoveCommand(MoveMode mode, MoveType type, Double azAmount, Double elAmount) {
		this.mode = mode;
		this.type = type;
		this.azAmount = azAmount;
		this.elAmount = elAmount;
	}
	
	/**
	 * Represents relative or absolute motion.
	 */
	public MoveMode getMode(){ return mode;}
	
	/**
	 * Represents the units of amount.  Either encoder pulses or degrees.
	 */
	public MoveType getType() { return type;}
	public Double getAzAmount() {return azAmount;}
	public Double getElAmount() {return elAmount;}
	public Double getAmount(Axis axis) {
		switch (axis) {
			case AZ:
				return azAmount;
			case EL:
				return elAmount;
			default:
				assert false; //This can only be reached if another axis is added.
		}
		return null;
	}
}
