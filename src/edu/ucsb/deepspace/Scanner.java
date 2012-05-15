package edu.ucsb.deepspace;



public class Scanner {

	private Stage stage;
	private ActInterface az, el;

	public Scanner(Stage stage, ActInterface az, ActInterface el) {
		this.stage = stage;
		this.az = az;
		this.el = el;
	}

	public void singleAxisScan() {

	}

	public void rasterScan(double minAz, double maxAz, double minEl, double maxEl) {
		double line = 0;
		//find bottom left corner(MinEl, MinEl)
		Coordinate bottom_left = new Coordinate(minAz, minEl);

		//find top right corner(MaxAz, MaxEl)
		Coordinate top_right = new Coordinate(maxAz, maxEl);


		//coordinate = bottom left
		Coordinate pos = bottom_left;
	


		//while pos.az!= MinAz, && pos.el!= maxEl
		//TODO define pos.equals to have a tolerance
		System.out.println(pos.getAz());
		
		while(!pos.compareAzAndEl(top_right, .001)){
			System.out.println("testing");

			//move from az of coordinate to minAZ
			System.out.println("moving to min az");
			stage.goToPos(pos);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//move el of coordinate up one line
			System.out.println("moving to max az");
			pos= new Coordinate(maxAz, pos.getY()+line);
			stage.goToPos(pos);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pos= new Coordinate(minAz, pos.getY()+line);
			
			
			


		}
		//loop exits
		//move diagonally to bottom left corner
		stage.goToPos(bottom_left);
		
	}




}