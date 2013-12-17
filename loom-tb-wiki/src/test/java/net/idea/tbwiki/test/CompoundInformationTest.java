package net.idea.tbwiki.test;

import java.util.Iterator;

import net.idea.loom.common.Bucket;
import net.idea.tbwiki.CompoundInformation;

import org.junit.Test;

public class CompoundInformationTest {

	@Test
	public void test() throws Exception {

		CompoundInformation cinf = new CompoundInformation("user","password");
		Bucket bucket = cinf.process("http://wiki.toxbank.net/wiki/Acetaminophen");
		Iterator keys = bucket.keySet().iterator();
		while (keys.hasNext()) {
			Object key = keys.next();
			System.out.println(key + "\t="+ bucket.get(key));
		}
	}
}
