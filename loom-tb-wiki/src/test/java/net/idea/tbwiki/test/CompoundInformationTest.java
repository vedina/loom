package net.idea.tbwiki.test;

import java.util.Iterator;

import net.idea.tbwiki.CompoundInformation;

import org.junit.Test;

public class CompoundInformationTest {

    @Test
    public void test() throws Exception {

	CompoundInformation cinf = new CompoundInformation("user", "password");
	BucketCallback bcallback = new BucketCallback();
	cinf.process("http://wiki.toxbank.net/wiki/Acetaminophen", bcallback);
	Iterator keys = bcallback.getBucket().keySet().iterator();
	while (keys.hasNext()) {
	    Object key = keys.next();
	    System.out.println(key + "\t=" + bcallback.getBucket().get(key));
	}
    }
}
