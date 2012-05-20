package edu.ucsb.deepspace;

public enum Axis {
	
	AZ("Azimuth", "A"),
	EL("Elevation", "B");

    private final String fullName;
    private final String abbrev;
    Axis(String fullName, String abbrev) {
        this.fullName = fullName;
        this.abbrev = abbrev;
    }
    public String getFullName() {return fullName;}
    public String getAbbrev() {return abbrev;}
	
}