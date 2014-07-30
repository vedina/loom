package net.idea.loom.nm.csv.test;

import net.idea.loom.nm.csv.CSV12Header;

import org.junit.Test;

public class CSVHeaderTest {
	@Test
	public void test() throws Exception {
		System.out.println("###File header");
		for (CSV12Header._lines line : CSV12Header._lines.values()) {
			System.out.println(line.getDescription());
		}
	}
}
