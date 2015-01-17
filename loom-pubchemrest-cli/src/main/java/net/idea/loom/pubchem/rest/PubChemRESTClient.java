package net.idea.loom.pubchem.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.idea.opentox.cli.AbstractClient;
import net.idea.opentox.cli.IIdentifiableResource;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.opentox.rest.RestException;

public class PubChemRESTClient<T extends IIdentifiableResource<String>> extends AbstractClient<String, T> {
    private static final String pubchem_rest = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/";

    protected enum command_input {
	substance
    };

    protected enum command_operation {
	synonyms
    };

    protected enum command_output {
	json
    };

    public PubChemRESTClient() throws Exception {
	super();
	httpClient = createHTTPClient();
	defaultMimeType = "application/json";
    }

    protected HttpClient createHTTPClient() throws Exception {
	return new DefaultHttpClient();
    }

    public void close() throws Exception {
	if (httpClient != null) {
	    httpClient.getConnectionManager().shutdown();
	    httpClient = null;
	}
    }

    @Override
    protected List<T> processPayload(InputStream in, String mediaType) throws RestException, IOException {
	return processPayloadWithCallback(in, mediaType, null);

    }

    protected List<T> processPayloadWithCallback(InputStream in, String mediaType, ICallback<List<T>> callback)
	    throws RestException, IOException {
	if (callback == null) {
	    callback = new ICallback<List<T>>() {
		@Override
		public List<T> processJSON(JsonNode node) {
		    System.out.println(node);
		    return null;
		}
	    };
	}
	if (mime_json.equals(mediaType)) {
	    ObjectMapper m = new ObjectMapper();
	    return callback.processJSON(m.readTree(in));
	} else
	    return super.processPayload(in, mediaType);
    }

    protected List<T> parseSynonyms(JsonNode node) {
	return null;
    }

    public List<T> getSubstanceSynonyms(String pubchem_sid) throws RestException, IOException {
	ICallback<List<T>> callback = new SubstanceSynonymsCallback();
	return get(String.format("%s/%s/sid/%s/%s/%s", pubchem_rest, command_input.substance.name(), pubchem_sid,
		command_operation.synonyms.name(), command_output.json.name()), mime_json, callback, null);
    }

    @Override
    protected List<T> get(String url, String mediaType, String... params) throws RestException, IOException {
	return get(url, mediaType, null, params);
    }

    protected List<T> get(String url, String mediaType, ICallback<List<T>> callback, String... params)
	    throws RestException, IOException {
	String address;
	try {
	    address = prepareParams(new URI(url), params);
	} catch (URISyntaxException x) {
	    throw new IOException(x);
	}
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
		return processPayloadWithCallback(in, mediaType, callback);

	    } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
		return Collections.emptyList();
	    } else
		throw new RestException(response.getStatusLine().getStatusCode(), response.getStatusLine()
			.getReasonPhrase());

	} finally {
	    try {
		if (in != null)
		    in.close();
	    } catch (Exception x) {
	    }
	}

    }

    protected String prepareParams(URI url, String... params) {
	String address = url.toString();
	if (params != null) {
	    StringBuilder b = new StringBuilder();
	    String d = url.getQuery() == null ? "?" : "&";
	    for (int i = 0; i < params.length; i += 2) {
		if ((i + 1) >= params.length)
		    break;
		b.append(String.format("%s%s=%s", d, params[i], URLEncoder.encode(params[i + 1])));
		d = "&";
	    }
	    address = String.format("%s%s", address, b);
	}
	return address;
    }
}

/*
 * { "InformationList": { "Information": [ { "SID": 144208813, "Synonym": [
 * "Anise oil", "DSSTox_RID_77444", "DSSTox_GSID_24528", "DSSTox_CID_1000000",
 * "Tox21_201615", "NCGC00259164-01", "CAS-8007-70-3" ] } ] } }
 */
class SubstanceSynonymsCallback<T extends IIdentifiableResource<String>> implements ICallback<List<T>> {

    @Override
    public List<T> processJSON(JsonNode node) {
	List<T> list = new ArrayList<T>();
	try {
	    ArrayNode info = (ArrayNode) node.get("InformationList").get("Information");
	    for (int i = 0; i < info.size(); i++) {
		JsonNode item = info.get(i);
		try {
		    T id = (T) new Identifier("PUBCHEM_SID", Integer.toString(item.get("SID").getIntValue()));
		    list.add(id);
		} catch (Exception x) {
		}
		ArrayNode synonyms = (ArrayNode) item.get("Synonym");
		for (int j = 0; j < synonyms.size(); j++) {
		    String type = "PUBCHEM Name";
		    String value = synonyms.get(j).getTextValue();
		    if (value.startsWith("DSSTox_RID_")) {
			type = "DSSTox_RID";
			value = value.substring(11);
		    } else if (value.startsWith("DSSTox_GSID_")) {
			type = "DSSTox_GSID";
			value = value.substring(12);
		    } else if (value.startsWith("DSSTox_CID_")) {
			type = "DSSTox_CID";
			value = value.substring(11);
		    } else if (value.startsWith("Tox21_")) {
			type = "Tox21";
			value = value.substring(6);
		    } else if (value.startsWith("CAS-")) {
			type = "CASRN";
			value = value.substring(4);
		    } else if (value.startsWith("NCGC")) {
			type = "NCGC";
		    } else {
			value = value.toLowerCase();
		    }
		    T id = (T) new Identifier(type, value);
		    list.add(id);
		}
	    }
	    return list;
	} catch (Exception x) {
	    return null;
	}
    }

}