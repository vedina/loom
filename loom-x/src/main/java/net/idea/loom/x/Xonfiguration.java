package net.idea.loom.x;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

public class Xonfiguration {
	static protected Hashtable<String, Properties> properties = new Hashtable<String, Properties>();

	public static synchronized String getProperty(String name) {
		return getProperty(name,"net/idea/loom/x/settings.properties");
	}
	static synchronized String getProperty(String name, String config) {
		try {
			Properties p = properties.get(config);
			if (p == null) {
				p = new Properties();
				InputStream in = Xonfiguration.class.getClassLoader().getResourceAsStream(config);
				p.load(in);
				in.close();
				properties.put(config, p);
			}
			return p.getProperty(name);

		} catch (Exception x) {
			return null;
		}
	}
}
