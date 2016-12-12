package net.idea.ops.cli;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.opentox.rest.RestException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import net.idea.loom.exceptions.NotSupportedMethodException;
import net.idea.opentox.cli.AbstractURIClient;
import net.idea.opentox.cli.IIdentifiableResource;
import net.idea.opentox.cli.id.IIdentifier;
import net.idea.opentox.cli.task.RemoteTask;

public abstract class AbstractOPSClient<T extends IIdentifiableResource<IIdentifier>>
		extends AbstractURIClient<T,String> {
	// protected String server_root = "https://beta.openphacts.org/";
	protected String server_root = "https://beta.openphacts.org/1.3";

	public String getServer_root() {
		return server_root;
	}

	public void setServer_root(String server_root) {
		this.server_root = server_root;
	}

	protected String resource;
	protected Hashtable<String, String> parameters = new Hashtable<String, String>();
	protected String api_version = null;

	public enum _format {
		json, tsv, ttl, xml, rdf, rdfjson, html
	}

	public enum _metadata {
		execution, site, formats, views, all
	}

	public enum resultOptions {
		Limit, Start, Length
	};

	public AbstractOPSClient(HttpClient httpclient) {
		super(httpclient);
	}

	public AbstractOPSClient() {
		super(null);
	}

	@Override
	public List<T> get(IIdentifier url) throws Exception {
		return this.get(url, mime_json, (String[]) null);
	}

	@Override
	protected List<T> get(IIdentifier url, String mediaType)
			throws RestException, IOException {
		return this.get(url, mediaType, (String[]) null);
	}

	@Override
	public List<T> get(IIdentifier url, String mediaType, String... params)
			throws RestException, IOException {
		return super.get(url, mediaType, extendParams(params));
	}

	@Override
	public List<IIdentifier> listURI(IIdentifier url) throws RestException,
			IOException {
		return this.listURI(url, (String[]) null);
	}

	@Override
	public List<IIdentifier> listURI(IIdentifier url, String... params)
			throws RestException, IOException {
		return super.listURI(url, extendParams(params));
	}

	protected String[] extendParams(String... params) {
		String[] newparams = new String[4 + (params == null ? 0 : params.length)];
		newparams[0] = OPSClient.keys.app_id.name();
		newparams[1] = getParameter(OPSClient.keys.app_id.name());
		newparams[2] = OPSClient.keys.app_key.name();
		newparams[3] = getParameter(OPSClient.keys.app_key.name());
		if (params != null)
			for (int i = 0; i < params.length; i++)
				newparams[4 + i] = params[i];
		return newparams;
	}

	@Override
	public void delete(T object) throws Exception {
		throw new NotSupportedMethodException();
	};

	@Override
	public void delete(IIdentifier url) throws Exception {
		throw new NotSupportedMethodException();
	};

	public T put(T object, java.util.List<String> accessRights)
			throws Exception {
		throw new NotSupportedMethodException();
	};

	public net.idea.opentox.cli.task.RemoteTask putAsync(T object)
			throws Exception {
		throw new NotSupportedMethodException();
	};

	@Override
	protected RemoteTask sendAsync(IIdentifier target, HttpEntity entity,
			String method) throws Exception {
		throw new NotSupportedMethodException();
	};

	@Override
	public T post(T object, IIdentifier collection) throws Exception {
		throw new NotSupportedMethodException();
	};

	@Override
	public T post(T object, IIdentifier collection,
			java.util.List<String> accessRights) throws Exception {
		throw new NotSupportedMethodException();
	};

	public String getParameter(String key) {
		return parameters.get(key);
	}

	public void setParameter(String key, String value) {
		if (value == null)
			parameters.remove(key);
		else
			parameters.put(key, value);
	}

	protected Integer getCount(String field, IIdentifier url, String mediaType,
			String... params) throws RestException, IOException {
		String address = prepareParams(url, extendParams(params));
		HttpGet httpGet = new HttpGet(address);
		if (headers != null)
			for (Header header : headers)
				httpGet.addHeader(header);
		httpGet.addHeader("Accept", mediaType);
		httpGet.addHeader("Accept-Charset", "utf-8");

		InputStream in = null;
		try {
			HttpResponse response = getHttpClient().execute(httpGet);
			HttpEntity entity = response.getEntity();
			in = entity.getContent();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				/*
				 * Model model = ModelFactory.createDefaultModel();
				 * model.read(new InputStreamReader(in,"UTF-8"),OpenTox.URI);
				 * return getIOClass().fromJena(model);
				 */
				return parseCount(in, mediaType, field);

			} else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				return null;
			} else
				throw new RestException(response.getStatusLine()
						.getStatusCode(), response.getStatusLine()
						.getReasonPhrase());

		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception x) {
			}
		}
	}

	protected Integer parseCount(InputStream in, String mediaType, String field)
			throws RestException, IOException {
		if (mime_json.equals(mediaType)) {
			ObjectMapper m = new ObjectMapper();
			JsonNode node = m.readTree(in);
			JsonNode format = (JsonNode) node.get("format");
			if (!"linked-data-api".equals(format.textValue()))
				return null;
			JsonNode result = node.get("result");
			JsonNode uri = result.get("primaryTopic");
			try {
				return uri.get(field).intValue();
			} catch (Exception x) {
				throw new IOException(x);
			}
		}
		throw new RestException(HttpStatus.SC_OK, "parsing not implemented "
				+ mediaType);
	}

	@Override
	public List<T> processPayload(InputStream in, String mediaType)
			throws RestException, IOException {
		List<T> list = null;
		if (mime_json.equals(mediaType)) {
			ObjectMapper m = new ObjectMapper();
			JsonNode node = m.readTree(in);
			JsonNode format = (JsonNode) node.get("format");
			if (!"linked-data-api".equals(format.textValue()))
				return null;
			JsonNode version = (JsonNode) node.get("version");
			api_version = version == null ? "1.2" : version.textValue();
			JsonNode result = node.get("result");
			JsonNode uri = result.get("items");
			if (uri instanceof ArrayNode) {
				ArrayNode items = ((ArrayNode) uri);
				list = new ArrayList<T>();
				for (int i = 0; i < items.size(); i++) {
					list.add(parseItem(m, result, items.get(i)));
				}
			}
			return list;
		} else
			return super.processPayload(in, mediaType);
	}

	protected abstract T parseItem(ObjectMapper m, JsonNode result,
			JsonNode item) throws MalformedURLException;
}
