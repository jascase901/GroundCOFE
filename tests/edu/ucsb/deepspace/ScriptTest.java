package edu.ucsb.deepspace;

import junit.framework.TestCase;

import org.junit.Test;

public class ScriptTest extends TestCase {
	private Script homeB;
	
	@Override 
	protected void setUp() {
		homeB = new Script("#HOMEB", 5);
	}
	@Test
	public void testAdd() {
		homeB.add("BG");
		homeB.add("EN");
		assertEquals("DL 5\r#HOMEB\rBG\rEN\r\\\r", homeB.getScript());
	}
	
	/*@Test
	public void testInit() {
		assertEquals("DL\r#HOMEB\rEN\\\r", homeB.getScript());
		
	}
	*/
	
//	@Test
//	public void testPlsfail() {
//		fail("NOT IMPLEMENTED");
//	}

}
