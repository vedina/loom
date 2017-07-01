package net.idea.i6.cli;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHeader;

import net.idea.iuclid.cli.IContainerClient;
import net.idea.iuclid.cli.IQueryToolClient;
import net.idea.iuclid.cli.IUCLIDLightClient;
import net.idea.opentox.cli.ApplicationClient;

public class I6LightClient extends ApplicationClient<I6Credentials> implements IUCLIDLightClient {
	protected String baseURL;

	public I6LightClient(String baseURL) {
		this.baseURL = baseURL;
	}

	@Override
	protected void token2header(HttpRequest request, I6Credentials token) {
		if (token != null) {
			request.addHeader("iuclid6-user", token.getUser());
			request.addHeader("iuclid6-pass", token.getPass());
		}
	}

	/**
	 * IUCLID6 REST services do not have "session"
	 */
	@Override
	public boolean login(String username, String password) throws Exception {
		if (ssoToken == null)
			ssoToken = new I6Credentials(username, password);
		else {
			ssoToken.setUser(username);
			ssoToken.setPass(password);
		}
		return username != null && password != null;
	}

	@Override
	public void logout() throws Exception {

	}

	public IContainerClient getContainerClient() {
		I6ContainerClient cli = new I6ContainerClient(getHttpClient(), baseURL, ssoToken);
		cli.setHeaders(new Header[] { new BasicHeader("iuclid6-user", ssoToken.getUser()),
				new BasicHeader("iuclid6-pass", ssoToken.getPass()) });
		return cli;
	}

	public IQueryToolClient getQueryToolClient() {
		// return new QueryToolClient(getHttpClient(), baseURL, ssoToken);
		return null;
	}

}

class I6Credentials {
	public I6Credentials(String user, String pass) {
		setUser(user);
		setPass(pass);
	}

	protected String user;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	protected String pass;
}
