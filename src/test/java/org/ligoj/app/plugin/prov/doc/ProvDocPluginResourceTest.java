/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.app.plugin.prov.doc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.app.AbstractServerTest;
import org.ligoj.app.iam.model.CacheCompany;
import org.ligoj.app.iam.model.CacheUser;
import org.ligoj.app.model.DelegateNode;
import org.ligoj.app.model.Node;
import org.ligoj.app.model.Parameter;
import org.ligoj.app.model.ParameterValue;
import org.ligoj.app.model.Project;
import org.ligoj.app.model.Subscription;
import org.ligoj.app.plugin.prov.doc.catalog.DocPriceImport;
import org.ligoj.app.plugin.prov.model.ProvLocation;
import org.ligoj.app.plugin.prov.model.ProvQuote;
import org.ligoj.app.resource.subscription.SubscriptionResource;
import org.ligoj.bootstrap.MatcherUtil;
import org.ligoj.bootstrap.core.curl.CurlRequest;
import org.ligoj.bootstrap.core.resource.BusinessException;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.tomakehurst.wiremock.matching.EqualToPattern;

/**
 * Test class of {@link ProvDocPluginResource}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
class ProvDocPluginResourceTest extends AbstractServerTest {

	protected int subscription;

	@Autowired
	private ProvDocPluginResource resource;

	@Autowired
	private SubscriptionResource subscriptionResource;

	@Autowired
	private ConfigurationResource configuration;

	@BeforeEach
	void prepareData() throws IOException {
		persistSystemEntities();
		persistEntities("csv",
				new Class<?>[] { Node.class, Project.class, CacheCompany.class, CacheUser.class, DelegateNode.class,
						Subscription.class, ProvLocation.class, ProvQuote.class, Parameter.class,
						ParameterValue.class },
				StandardCharsets.UTF_8);
		configuration.put("service:prov:digitalocean:api", "http://localhost:" + MOCK_PORT + "/");
		this.subscription = getSubscription("Jupiter");

		// Invalidate digitalocean cache
		cacheManager.getCache("curl-tokens").clear();
	}

	@Test
	void getKey() {
		Assertions.assertEquals("service:prov:digitalocean", resource.getKey());
	}

	@Test
	void getName() {
		Assertions.assertEquals("Digital Ocean", resource.getName());
	}

	@Test
	void install() throws IOException {
		final var resource2 = new ProvDocPluginResource();
		resource2.priceImport = Mockito.mock(DocPriceImport.class);
		resource2.install();
	}

	@Test
	void updateCatalog() throws IOException {
		// Re-Install a new configuration
		final var resource2 = new ProvDocPluginResource();
		super.applicationContext.getAutowireCapableBeanFactory().autowireBean(resource2);
		resource2.priceImport = Mockito.mock(DocPriceImport.class);
		resource2.updateCatalog("service:prov:digitalocean:test", false);
	}

	@Test
	void updateCatalogNoRight() {
		initSpringSecurityContext("any");

		// Re-Install a new configuration
		Assertions.assertEquals("read-only-node", Assertions.assertThrows(BusinessException.class, () -> resource.updateCatalog("service:prov:digitalocean:test", false)).getMessage());
	}

	@Test
	void create() throws Exception {
		prepareMockAuth();
		resource.create(subscription);
	}

	/**
	 * Return the subscription identifier of the given project. Assumes there is only one subscription for a service.
	 */
	private int getSubscription(final String project) {
		return getSubscription(project, ProvDocPluginResource.KEY);
	}

	private void prepareMockAuth() throws IOException {
		configuration.put(ProvDocPluginResource.CONF_API_URL, "http://localhost:" + MOCK_PORT);
		httpServer.stubFor(get(urlEqualTo("/projects"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody(IOUtils.toString(
						new ClassPathResource("mock-server/digitalocean/projects.json").getInputStream(), StandardCharsets.UTF_8))));
		httpServer.start();
	}

	@Test
	void checkStatus() throws Exception {
		prepareMockAuth();
		Assertions.assertTrue(resource.checkStatus(subscriptionResource.getParametersNoCheck(subscription)));
	}

	/**
	 * Authority error, client side
	 */
	@Test
	void checkStatusAuthorityError() {
		configuration.put(ProvDocPluginResource.CONF_API_URL, "http://localhost:" + MOCK_PORT);
		var param = subscriptionResource.getParametersNoCheck(subscription);
		MatcherUtil.assertThrows(
				Assertions.assertThrows(ValidationJsonException.class, () -> resource.checkStatus(param)),
				ProvDocPluginResource.PARAMETER_TOKEN, "digitalocean-login");
	}

	@Test
	void processor() {
		httpServer.stubFor(get(urlPathEqualTo("/")).withHeader("Authorization", new EqualToPattern("Bearer TOKEN"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)));
		httpServer.start();
		try (var curl = new DocCurlProcessor()) {
			curl.setToken("TOKEN");
			Assertions.assertTrue(curl.process(new CurlRequest("GET", "http://localhost:" + MOCK_PORT + "/")));
		}
	}

	@Test
	void getVersion() throws Exception {
		Assertions.assertEquals("2", resource.getVersion(subscription));
	}

}
