package net.idea.i6.cli;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.opentox.rest.RestException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.idea.iuclid.cli.IContainerClient;
import net.idea.iuclid.cli.IUCLIDAbstractClient;
import net.idea.opentox.cli.IIdentifiableResource;
import net.idea.opentox.cli.id.IIdentifier;

public class I6ContainerClient extends IUCLIDAbstractClient<I6Credentials> implements IContainerClient {

	public I6ContainerClient(HttpClient httpclient, String baseURL, I6Credentials token) {
		super(httpclient, baseURL, token);
	}

	@Override
	protected List<IIdentifiableResource<IIdentifier>> get(IIdentifier identifier, String mediaType, String... params)
			throws RestException, IOException {

		HttpPost httpPOST = new HttpPost(String.format("%s/raw/SUBSTANCE/%s/export", baseURL, identifier.toString()));

		if (headers != null)
			for (Header header : headers)
				httpPOST.addHeader(header);

		// httpPOST.addHeader("Accept-Encoding", "gzip,deflate");
		httpPOST.addHeader("Content-type", "application/vnd.iuclid6.ext+json; type=iuclid6.FullExport");
		httpPOST.addHeader("Accept", "application/vnd.iuclid6.ext+json; type=iuclid6.Iuclid6Job");

		HttpEntity content = new StringEntity("{}", "UTF-8");
		httpPOST.setEntity(content);

		logger.log(Level.INFO, httpPOST.getURI().toString());
		logger.log(Level.INFO, Arrays.toString(httpPOST.getAllHeaders()));
		try {
			HttpResponse response = getHttpClient().execute(httpPOST);
			HttpEntity entity = response.getEntity();

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK || response.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED) {
				try (InputStream in = entity.getContent()) {
					// return processPayload(in, identifier.toString());
					String resultURI = null;
					ObjectMapper m = new ObjectMapper();
					JsonNode node = m.readTree(in);
					String uri = node.get("uri").asText();
					if (!SUCCEEDED.equals(node.get("status"))) {
						if (polling(uri))
							resultURI = uri + "/result";
						else
							resultURI = null;
					} else
						resultURI = uri + "/result";

					if (resultURI == null)
						return Collections.emptyList();

					HttpGet result = new HttpGet(uri);
					if (headers != null)
						for (Header header : headers)
							result.addHeader(header);
					HttpResponse resultresponse = getHttpClient().execute(result);
					try (InputStream inr = resultresponse.getEntity().getContent()) {

						return processPayload(inr, identifier.toString());
					}

				} catch (Exception x) {
					x.printStackTrace();
				}

			} else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				return Collections.emptyList();

			} else
				throw new RestException(response.getStatusLine().getStatusCode(),
						response.getStatusLine().getReasonPhrase());
			return null;
		} finally {

		}
	}

	private final static String SUCCEEDED = "SUCCEEDED";

	protected boolean polling(String uri) throws RestException, IOException {
		ObjectMapper m = new ObjectMapper();
		HttpGet polling = new HttpGet(String.format("%s/system%s",baseURL,uri));
		polling.addHeader("Accept", "accept:application/vnd.iuclid6.ext+json; type=iuclid6.Iuclid6Job");
		if (headers != null)
			for (Header header : headers)
				polling.addHeader(header);
		boolean waiting = true;
		while (waiting) {

			HttpResponse response = getHttpClient().execute(polling);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED)
				continue;

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				try (InputStream in = response.getEntity().getContent()) {
					JsonNode node = m.readTree(in);
					if (SUCCEEDED.equals(node.get("status")))
						return true;
				}
			}
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNPROCESSABLE_ENTITY)
				return false;
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR)
				return false;
			logger.log(Level.WARNING, response.getStatusLine().toString());
			break;
		}
		return false;
	}

	@Override
	public List<IIdentifiableResource<IIdentifier>> processPayload(InputStream in, String identifier)
			throws RestException, IOException {
		File tmpFile = File.createTempFile("i6ws_", ".i6z");
		net.idea.loom.common.DownloadTool.download(in, tmpFile);
		return null;
	}

}
