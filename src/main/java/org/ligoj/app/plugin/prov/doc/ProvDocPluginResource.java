/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.app.plugin.prov.doc;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ligoj.app.plugin.prov.AbstractProvResource;
import org.ligoj.app.plugin.prov.ProvResource;
import org.ligoj.app.plugin.prov.catalog.ImportCatalogService;
import org.ligoj.app.plugin.prov.doc.catalog.DocPriceImport;
import org.ligoj.app.plugin.prov.model.VmOs;
import org.ligoj.bootstrap.core.curl.CurlRequest;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The provisioning service for Digital Ocean. There is complete quote configuration along the subscription.
 */
@Service
@Path(ProvDocPluginResource.SERVICE_URL)
@Produces(MediaType.APPLICATION_JSON)
public class ProvDocPluginResource extends AbstractProvResource implements ImportCatalogService {

	/**
	 * Plug-in Key shortcut
	 */
	public static final String PLUGIN_KEY = "service:prov:digitalocean";

	/**
	 * Plug-in key.
	 */
	public static final String SERVICE_URL = ProvResource.SERVICE_URL + "/digitalocean";

	/**
	 * Plug-in key.
	 */
	public static final String KEY = SERVICE_URL.replace('/', ':').substring(1);

	/**
	 * Default management URL end-point.
	 */
	private static final String DEFAULT_API_URL = "https://api.digitalocean.com/v2";

	/**
	 * API Management URL.
	 */
	protected static final String CONF_API_URL = PLUGIN_KEY + ":api";

	/**
	 * A valid API key. Would be used to retrieve a access token.
	 */
	public static final String PARAMETER_TOKEN = PLUGIN_KEY + ":access-token";

	@Autowired
	protected DocPriceImport priceImport;

	@Autowired
	protected ConfigurationResource configuration;

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getVersion(final Map<String, String> parameters) {
		// Use API version as product version
		return "2";
	}

	/**
	 * Execute an API operation for the check.
	 *
	 * @param parameters The subscription parameters.
	 * @param processor  The processor used to authenticate and execute the request.
	 */
	protected void authenticate(final Map<String, String> parameters, final DocCurlProcessor processor) {
		processor.setToken(parameters.get(PARAMETER_TOKEN));
		if (!processor.process(new CurlRequest("GET", getApiUrl() + "/projects"))) {
			throw new ValidationJsonException(PARAMETER_TOKEN, "digitalocean-login");
		}
	}

	/**
	 * Return the management URL.
	 *
	 * @return The management URL.
	 */
	protected String getApiUrl() {
		return configuration.get(CONF_API_URL, DEFAULT_API_URL);
	}

	/**
	 * Check the server is available with enough permission to query VM. Requires "VIRTUAL MACHINE CONTRIBUTOR"
	 * permission.
	 *
	 * @param parameters The subscription parameters.
	 */
	protected void validateAdminAccess(final Map<String, String> parameters) {
		authenticate(parameters, new DocCurlProcessor());
	}

	@Override
	public boolean checkStatus(final Map<String, String> parameters) {
		// Status is UP <=> Administration access is UP (if defined)
		validateAdminAccess(parameters);
		return true;
	}

	/**
	 * Fetch the prices from the DigitalOcean server. Install or update the prices
	 */
	@Override
	public void install() throws IOException {
		priceImport.install(false);
	}

	@Override
	public void updateCatalog(final String node, final boolean force) throws IOException {
		// Digital Ocean catalog is shared with all instances, require tool level access
		nodeResource.checkWritableNode(KEY);
		priceImport.install(force);
	}

	@Override
	public void create(final int subscription) {
		// Authenticate only for the check
		authenticate(subscriptionResource.getParameters(subscription), new DocCurlProcessor());
	}

	@Override
	public VmOs getCatalogOs(final VmOs os) {
		return os.toPricingOs() == VmOs.LINUX ? VmOs.CENTOS : super.getCatalogOs(os);
	}
}
