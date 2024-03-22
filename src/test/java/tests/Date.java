package tests;

import java.text.ParseException;

import org.testng.annotations.Test;

import utilities.Utilities;

public class Date {

	@Test
	public void test() throws ParseException {
		
		System.out.println(Utilities.formatData("2024-03-22 12:57:41.000"));
	}
}
