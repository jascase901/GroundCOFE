package edu.ucsb.deepspace.tcp;

import static org.junit.Assert.*;

import org.junit.Test;

public class ParseTest {

	@Test
	public void getScriptTest() {
		String script = "move(1,2);";
		Parser parser = new Parser("move(1,2); ");
		String parsed = parser.getScript();
		assertEquals(script, parsed);
	}
	@Test
	public void getScriptMultiTest(){
		String script = "move(1,2);move(2,1);delay(3);";
		Parser parser = new Parser("move(1,2); move(2,1); delay(3);");
		String parsed = parser.getScript();
		assertEquals(script, parsed);
	}
	
	@Test
	
	public void getParsedTest(){
		String script = "stage.gotoPos(1,2);";
		Parser parser = new Parser("move(1,2); ");
		String parsed = parser.getParsed();
		assertEquals(script, parsed);
		
	}
	
	@Test
	public void getParsedMultiTest(){
		String script = "stage.gotoPos(1,2);stage.gotoPos(2,1);stage.pause(3);";
		Parser parser = new Parser("move(1,2); move(2,1); delay(3);");
		String parsed = parser.getParsed();
		assertEquals(script, parsed);
	}
	


}
