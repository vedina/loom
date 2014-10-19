package net.idea.ops.cli;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.idea.opentox.cli.structure.Compound;
import net.idea.ops.cli.assay.Pathway;
import net.idea.ops.cli.compound.OPSCompoundClient;
import net.idea.ops.cli.pharmacology.OPSPathwayClient;
import net.idea.ops.cli.pharmacology.OPSPharmacologyClient;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.opentox.rest.RestException;

/**
 * Top level OpenTox API client.
 * @author nina
 *
 */
public class OPSClient {
	private static final String ops_server_root ="ops.server_root";
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
	protected boolean trustallcerts = false;
	
	public OPSClient(boolean trustallcerts) throws Exception {
		super();
		this.trustallcerts = trustallcerts;
		httpClient = createHTTPClient();
	}
	public HttpClient getHttpClient()throws Exception   {
		if (httpClient==null) httpClient = createHTTPClient();
		return httpClient;
	}

	protected HttpClient createHTTPClient() throws Exception {
		if (trustallcerts) {
			ClientConnectionManager cm = createFullyTrustingClientManager();
			return new DefaultHttpClient(cm);
		} else return new DefaultHttpClient();
	}
	

	  private ClientConnectionManager createFullyTrustingClientManager() throws Exception {
	    TrustManager[] trustAllCerts = new TrustManager[]{
	        new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            public void checkClientTrusted(
	                java.security.cert.X509Certificate[] certs, String authType) {                
	            }
	            public void checkServerTrusted(
	                java.security.cert.X509Certificate[] certs, String authType) {
	            }
	        }
	    };
	    SSLContext sslContext = SSLContext.getInstance("SSL");
	    sslContext.init(null, trustAllCerts, new SecureRandom());
	    
	    SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslContext, new X509HostnameVerifier() {
	      public void verify(String host, SSLSocket ssl) throws IOException { }
	      public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException { }
	      public boolean verify(String arg0, SSLSession arg1) {
	        return true;
	      }
	      @Override
	      public void verify(String host, java.security.cert.X509Certificate cert)
	          throws SSLException {
	      }
	    });
	    Scheme httpsScheme = new Scheme("https", 443, sslSocketFactory);
	    Scheme httpScheme = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
	    SchemeRegistry schemeRegistry = new SchemeRegistry();
	    schemeRegistry.register(httpsScheme);
	    schemeRegistry.register(httpScheme);
	    
	    ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);

	    return cm;
	  }  	
	
	public void close() throws Exception {
		if (httpClient !=null) {
			httpClient.getConnectionManager().shutdown();
			httpClient = null;
		}
	}
	
	public OPSCompoundClient getCompoundClient() throws Exception {
		OPSCompoundClient cli =  new OPSCompoundClient(getHttpClient());
		cli.setServer_root(properties.get(ops_server_root).toString());
		cli.setParameter(keys.app_id.name(),properties.get(keys.app_id.name()).toString());
		cli.setParameter(keys.app_key.name(),properties.get(keys.app_key.name()).toString());
		return cli;
	}
	
	
	public OPSPharmacologyClient getPharmacologyClient() throws Exception {
		OPSPharmacologyClient cli =  new OPSPharmacologyClient(getHttpClient());
		cli.setServer_root(properties.get(ops_server_root).toString());
		cli.setParameter(keys.app_id.name(),properties.get(keys.app_id.name()).toString());
		cli.setParameter(keys.app_key.name(),properties.get(keys.app_key.name()).toString());
		return cli;
	}
	

	public OPSPathwayClient getPathwayClient() throws Exception {
		OPSPathwayClient cli =  new OPSPathwayClient(getHttpClient());
		cli.setServer_root(properties.get(ops_server_root).toString());
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
	
	public Integer getPathwaysByCompoundCount(Compound cmp) throws Exception {
		if (cmp.getResourceIdentifier()==null) {
			if (cmp.getInChIKey()==null) throw new RestException(HttpStatus.SC_BAD_REQUEST);
			OPSCompoundClient cli = getCompoundClient();
			List<Compound> list = cli.searchStructuresByInchikey(cmp.getInChIKey());
			for (Compound l : list) cmp.setResourceIdentifier(l.getResourceIdentifier());
		}
		if (cmp.getResourceIdentifier()!=null) {
			OPSPathwayClient cli = new OPSPathwayClient();
			return cli.getPathwaysByCompoundCount(cmp);
		}
		return null;
	}
	//"BSYNRYMUTXBXSQ-UHFFFAOYSA-N"
	public List<Pathway> getPathwaysByCompound(Compound cmp) throws Exception {
		if (cmp.getResourceIdentifier()==null) {
			if (cmp.getInChIKey()==null) throw new RestException(HttpStatus.SC_BAD_REQUEST);
			OPSCompoundClient cli = getCompoundClient();
			List<Compound> list = cli.searchStructuresByInchikey(cmp.getInChIKey());
			for (Compound l : list) cmp.setResourceIdentifier(l.getResourceIdentifier());
		}
		if (cmp.getResourceIdentifier()!=null) {
			OPSPathwayClient cli = new OPSPathwayClient();
			return cli.getPathwaysByCompound(cmp);
		}
		return null;
	}
	
	
	public Integer getPharmacologyByCompoundCount(String inchikey) throws Exception {
		if (inchikey==null) throw new RestException(HttpStatus.SC_BAD_REQUEST);
		OPSCompoundClient cli = getCompoundClient();
		List<Compound> list = cli.searchStructuresByInchikey(inchikey);
		OPSPharmacologyClient pcli = getPharmacologyClient();
		for (Compound l : list) {
			return pcli.getCompoundPharmacologyCount(l);
		}
		return null;
	}
	
	public Integer getPathwaysByCompoundCount(String inchikey) throws Exception {
		if (inchikey==null) throw new RestException(HttpStatus.SC_BAD_REQUEST);
		OPSCompoundClient cli = getCompoundClient();
		List<Compound> list = cli.searchStructuresByInchikey(inchikey);
		OPSPathwayClient pcli = getPathwayClient();
		for (Compound l : list) {
			return pcli.getPathwaysByCompoundCount(l);
		}
		return null;
	}
	
	public int[] getCountsbyCompound(String inchikey) throws Exception {
		if (inchikey==null) throw new RestException(HttpStatus.SC_BAD_REQUEST);
		OPSCompoundClient cli = getCompoundClient();
		List<Compound> list = cli.searchStructuresByInchikey(inchikey);
		OPSPathwayClient pcli = getPathwayClient();
		OPSPharmacologyClient ccli = getPharmacologyClient();
		int[] count = {0,0};
		for (Compound l : list) { 
			try {
				count[0] = pcli.getPathwaysByCompoundCount(l);
			} catch (Exception x) {}
			try {
				count[1] = ccli.getCompoundPharmacologyCount(l);
			} catch (Exception x) {}
		}	
		return count;
	}

	
}
