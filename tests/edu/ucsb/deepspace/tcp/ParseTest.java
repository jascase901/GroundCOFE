package edu.ucsb.deepspace.tcp;

import static org.junit.Assert.*;

import org.junit.Test;

public class ParseTest {

	@Test
	public void getScriptTest() {
		String script = "abs(1,2);";
		Parser parser = new Parser("abs(1,2); ");
		String parsed = parser.getScript();
		assertEquals(script, parsed);
	}
	@Test
	public void getScriptMultiTest(){
		String script = "abs(1,2);rel(2,1);delay(3);";
		Parser parser = new Parser("abs(1,2); rel(2,1); delay(3);");
		String parsed = parser.getScript();
		assertEquals(script, parsed);
	}
	
	@Test
	
	public void getParsedTest(){
		String script = "abs(1,2);";
		Parser parser = new Parser("abs(1,2); ");
		String parsed = parser.getParsed();
		assertEquals(script, parsed);
		
	}
	
	@Test
	public void getParsedMultiTest(){
		String script = "abs(1,2);rel(2,1);delay(3);";
		Parser parser = new Parser("abs(1,2); rel(2,1); delay(3);");
		String parsed = parser.getParsed();
		assertEquals(script, parsed);
	}
	


}
