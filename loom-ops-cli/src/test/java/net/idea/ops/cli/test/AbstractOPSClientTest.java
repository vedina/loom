package net.idea.ops.cli.test;

import java.util.Properties;

import net.idea.opentox.cli.AbstractURLResource;
import net.idea.ops.cli.AbstractOPSClient;
import net.idea.ops.cli.OPSClient;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractOPSClientTest<T extends AbstractURLResource, C extends AbstractOPSClient<T>> {
	public static OPSClient opscli;
	public final static String TEST_SERVER = config();
	//should be configured in the .m2/settings.xml 
	protected static Properties properties;

	@BeforeClass
	public static void setup() throws Exception {
		 opscli = new OPSClient(true);
	}
	

	@AfterClass
	public static void teardown() throws Exception {
		opscli.close();
	}
	
	public static String config()  {
		//String local = "https://beta.openphacts.org";
		String local = "https://beta.openphacts.org/";
		try {
			properties = OPSClient.config();
			String testServer = properties.getProperty("ops.server_root");
			return testServer!=null?testServer.startsWith("http")?testServer:local:local;
		} catch (Exception x) {
			return local;
		}
	}
	
	
	protected abstract C getOPSClient() throws Exception;
	
	
}
