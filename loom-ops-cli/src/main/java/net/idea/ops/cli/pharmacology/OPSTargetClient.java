package net.idea.ops.cli.pharmacology;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import net.idea.ops.cli.AbstractOPSClient;
import net.idea.ops.cli.OPSClient.pagination;
import net.idea.ops.cli.OPSClient.params;
import net.idea.ops.cli.assay.Target;

import org.apache.http.client.HttpClient;
import org.opentox.rest.RestException;

public class OPSTargetClient extends AbstractOPSClient<Target> {

	public OPSTargetClient() {
		this(null);
	}
		
	public OPSTargetClient(HttpClient httpclient) {
		super(httpclient);
		resource = "/target";
	}
	public List<Target> getTargetInfo(URL queryService,Target target) throws RestException,IOException {
		URL ref = new URL(String.format("%s%s",queryService,resource));
		return get(ref,mime_json,
				params.uri.name(),target.getResourceIdentifier().toExternalForm(),
				"_format",_format.json.name());
	}	
	@Override
	protected List<Target> processPayload(InputStream in, String mediaType)
			throws RestException, IOException {
		return null;
	}
}

