package net.idea.ops.cli;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;

import net.idea.opentox.cli.AbstractClient;
import net.idea.opentox.cli.IIdentifiableResource;
import net.idea.opentox.cli.task.RemoteTask;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.opentox.rest.RestException;

public abstract class AbstractOPSClient<T extends IIdentifiableResource<URL>> extends AbstractClient<T,String> {
	//protected String server_root = "https://beta.openphacts.org/";
	protected String server_root = "https://beta.openphacts.org/1.3/";
	protected String resource;
	protected Hashtable<String, String> parameters = new Hashtable<String, String>();


	public enum _format {
		json,
		tsv,
		ttl,
		xml,
		rdf,
		rdfjson,
		html
	}
	public enum _metadata {
		execution,
		site,
		formats,
		views,
		all
	}
	public enum resultOptions {
		Limit,
		Start,
		Length
	};
	public AbstractOPSClient(HttpClient httpclient) {
		super(httpclient);
	}
	
	public AbstractOPSClient() {
		super(null);
	}
	@Override
	public List<T> get(URL url) throws Exception {
		return this.get(url,mime_json,(String[]) null);
	}
	@Override
	protected List<T> get(URL url, String mediaType) throws RestException,
			IOException {
		return this.get(url, mediaType,(String[]) null);
	}
	@Override
	protected List<T> get(URL url, String mediaType, String... params)
			throws RestException, IOException {
		return super.get(url, mediaType, extendParams(params));
	}
	@Override
	public List<URL> listURI(URL url) throws RestException, IOException {
		return this.listURI(url,(String[]) null);
	}
	@Override
	public List<URL> listURI(URL url, String... params) throws RestException,
			IOException {
		return super.listURI(url, extendParams(params));
	}
	protected String[] extendParams(String... params) {
		String[] newparams = new String[4+(params==null?0:params.length)];
		newparams[0] = OPSClient.keys.app_id.name();
		newparams[1] = getParameter(OPSClient.keys.app_id.name());
		newparams[2] = OPSClient.keys.app_key.name();
		newparams[3] = getParameter(OPSClient.keys.app_key.name());
		if (params!=null)
			for (int i=0; i < params.length; i++ ) newparams[4+i] = params[i];
		return newparams;
	}
	@Override
	public void delete(T object) throws Exception { throw new NotSupportedMethodException();};
	@Override
	public void delete(URL url) throws Exception  { throw new NotSupportedMethodException();};
	public T put(T object, java.util.List<String> accessRights) throws Exception  { throw new NotSupportedMethodException();};
	public net.idea.opentox.cli.task.RemoteTask putAsync(T object) throws Exception  { throw new NotSupportedMethodException();};
	@Override
	protected RemoteTask sendAsync(URL target, HttpEntity entity, String method) throws Exception { throw new NotSupportedMethodException();};
	@Override
	public T post(T object, URL collection) throws Exception { throw new NotSupportedMethodException();};
	@Override
	public T post(T object, URL collection, java.util.List<String> accessRights) throws Exception { throw new NotSupportedMethodException();};
	
	public String getParameter(String key) {
		return parameters.get(key);
	}
	public void setParameter(String key,String value) {
		if (value==null) parameters.remove(key);
		else parameters.put(key,value);
	}

}
