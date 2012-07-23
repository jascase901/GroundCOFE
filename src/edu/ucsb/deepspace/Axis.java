package edu.ucsb.deepspace;

public enum Axis {
	
	AZ("Azimuth", "A", 1),
	EL("Elevation", "B", 1);

    private final String fullName;
    private final String abbrev;
    private int polarity;
    Axis(String fullName, String abbrev, int polarity) {
        this.fullName = fullName;
        this.abbrev = abbrev;
        this.polarity = polarity;
      
    }
    public String getFullName() {return fullName;}
    public String getAbbrev() {return abbrev;}
    public int getPolarity() {return polarity;}
	
}