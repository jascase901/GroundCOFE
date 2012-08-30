package edu.ucsb.deepspace.tcp;

import static org.junit.Assert.*;

import org.junit.Test;

public class InterpereterTest {

	@Test
	public void test() {
		String userCode = "time();";
		Interpereter interpereter = new Interpereter(userCode);
		String calledCode="Function:time Args:";
		userCode = interpereter.Interperet();
		assertEquals(calledCode,userCode);
	}
	@Test
	public void test2() {
		String userCode = "delay(20);";
		Interpereter interpereter = new Interpereter(userCode);
		String calledCode="Function:delay Args:1=20";
		userCode = interpereter.Interperet();
		assertEquals(calledCode, userCode);
	}
	
	@Test
	public void test3(){
		String userCode = "move(1,2)";
		Interpereter interpereter = new Interpereter(userCode);
		String calledCode="Function:move Args:2=1,2";
		userCode = interpereter.Interperet();
		assertEquals(calledCode, userCode);
	}

}
