package net.idea.loom.x.test;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.junit.Test;


public class TestExtract {
	@Test
	public void test() throws Exception {
	    //create parser as per desired parser
	    TikaConfig config;
	    String configfile = "configInceptionV3Net.xml";
	    //String configfile = "config_VGG16.xml";
	    try (InputStream stream = TestExtract.class.getClassLoader()
	            .getResourceAsStream(String.format("net/idea/loom/x/%s",configfile))){
	        config = new TikaConfig(stream);
	    }
	    Tika parser = new Tika(config);
	    
	    File imageFile = new File("DSC_0509.JPG");
	    //12.59.57[R][0@0][0].jpg
	    Metadata meta = new Metadata();
	    long now = System.currentTimeMillis();
	    parser.parse(imageFile, meta);
	    
	    System.out.println(Arrays.toString(meta.getValues("OBJECT")));
	    System.out.println(String.format("%s",System.currentTimeMillis()-now));
	}
}
