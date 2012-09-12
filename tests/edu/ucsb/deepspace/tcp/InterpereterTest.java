package edu.ucsb.deepspace.tcp;

import static org.junit.Assert.*;


import org.junit.Test;

import edu.ucsb.deepspace.StageInterface;

public class InterpereterTest {
	private StageInterface stage = new MockStage();

	@Test
	public void test() {
		String userCode = "time();";
		Interpereter interpereter = new Interpereter(userCode, stage);
		String calledCode="Function:time Args:";
		interpereter.Interperet();
		userCode = interpereter.getDebug();


				
		assertEquals(calledCode, userCode);
	}
	@Test
	public void test2() {
		String userCode = "delay(20);";
		Interpereter interpereter = new Interpereter(userCode, stage);
		String calledCode="Function:delay Args:1=20";
		interpereter.Interperet();
		userCode = interpereter.getDebug();		
		assertEquals(calledCode, userCode);
	}
	
	@Test
	public void test3(){
		String userCode = "abs(1,2);";
		Interpereter interpereter = new Interpereter(userCode, stage);
		String calledCode="Function:abs Args:2=1,2";
		interpereter.Interperet();
		userCode = interpereter.getDebug();
		assertEquals(calledCode, userCode);
	}
	
	@Test
	public void test4(){
		String userCode = "azscan(1,5,30,1);";
		Interpereter interpereter = new Interpereter(userCode, stage);
		String calledCode="Function:azscan Args:4=1,5,30,1";
		interpereter.Interperet();
		userCode = interpereter.getDebug();
		assertEquals(calledCode, userCode);
	}

}
