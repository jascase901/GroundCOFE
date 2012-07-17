package edu.ucsb.deepspace;

public class GalilCalc {
	static double calcAccel(double distance, double time){
		return round((2*distance)/(time*time), 4);
	}
	static double round(double number, int dec_place){
		return(Math.round(number*Math.pow(10,dec_place))/Math.pow(10, dec_place));
	}

}
