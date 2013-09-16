package net.idea.ops.cli;

import java.util.Properties;

import net.idea.ops.cli.compound.OPSCompoundClient;
import net.idea.ops.cli.compound.OPSPharmacologyClient;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Top level OpenTox API client.
 * @author nina
 *
 */
public class OPSClient {
	public enum keys {
		app_id,
		app_key
	}
	public enum pagination {
		_page,
		_pageSize
	}	
	public enum params {
		uri
	}
	protected HttpClient httpClient;
	protected Properties properties = config();
	
	public OPSClient() {
		super();
		httpClient = createHTTPClient();
	}
	public HttpClient getHttpClient() {
		if (httpClient==null) httpClient = createHTTPClient();
		return httpClient;
	}

	protected HttpClient createHTTPClient() {
		HttpClient cli = new DefaultHttpClient();
		return cli;
	}
	
	public void close() throws Exception {
		if (httpClient !=null) {
			httpClient.getConnectionManager().shutdown();
			httpClient = null;
		}
	}
	
	public OPSCompoundClient getCompoundClient() {
		OPSCompoundClient cli =  new OPSCompoundClient(getHttpClient());
		cli.setParameter(keys.app_id.name(),properties.get(keys.app_id.name()).toString());
		cli.setParameter(keys.app_key.name(),properties.get(keys.app_key.name()).toString());
		return cli;
	}
	
	
	public OPSPharmacologyClient getPharmacologyClient() {
		OPSPharmacologyClient cli =  new OPSPharmacologyClient(getHttpClient());
		cli.setParameter(keys.app_id.name(),properties.get(keys.app_id.name()).toString());
		cli.setParameter(keys.app_key.name(),properties.get(keys.app_key.name()).toString());
		return cli;
	}
	
	public static Properties config()  {
		try {
			Properties properties = new Properties();
			properties.load(OPSClient.class.getClassLoader().getResourceAsStream("net/idea/ops/cli/client.properties"));
			return properties;
		} catch (Exception x) {
			return null;
		}
	}
}
