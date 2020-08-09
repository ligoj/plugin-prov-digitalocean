/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.app.plugin.prov.doc.catalog;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.ligoj.app.plugin.prov.quote.instance.QuoteInstanceQuery.builder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.http.HttpStatus;
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
import org.ligoj.app.plugin.prov.ProvResource;
import org.ligoj.app.plugin.prov.QuoteVo;
import org.ligoj.app.plugin.prov.catalog.AbstractImportCatalogResource;
import org.ligoj.app.plugin.prov.catalog.ImportCatalogResource;
import org.ligoj.app.plugin.prov.dao.ProvQuoteRepository;
import org.ligoj.app.plugin.prov.doc.ProvDocPluginResource;
import org.ligoj.app.plugin.prov.model.ProvLocation;
import org.ligoj.app.plugin.prov.model.ProvQuote;
import org.ligoj.app.plugin.prov.model.ProvQuoteInstance;
import org.ligoj.app.plugin.prov.model.ProvQuoteStorage;
import org.ligoj.app.plugin.prov.model.ProvStorageOptimized;
import org.ligoj.app.plugin.prov.model.ProvTenancy;
import org.ligoj.app.plugin.prov.model.ProvUsage;
import org.ligoj.app.plugin.prov.model.Rate;
import org.ligoj.app.plugin.prov.model.SupportType;
import org.ligoj.app.plugin.prov.model.VmOs;
import org.ligoj.app.plugin.prov.quote.database.ProvQuoteDatabaseResource;
import org.ligoj.app.plugin.prov.quote.database.QuoteDatabaseQuery;
import org.ligoj.app.plugin.prov.quote.instance.ProvQuoteInstanceResource;
import org.ligoj.app.plugin.prov.quote.instance.QuoteInstanceEditionVo;
import org.ligoj.app.plugin.prov.quote.storage.ProvQuoteStorageResource;
import org.ligoj.app.plugin.prov.quote.storage.QuoteStorageEditionVo;
import org.ligoj.app.plugin.prov.quote.storage.QuoteStorageQuery;
import org.ligoj.app.plugin.prov.quote.support.ProvQuoteSupportResource;
import org.ligoj.bootstrap.core.resource.BusinessException;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test class of {@link DocPriceImport}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
class ProvDocPriceImportTest extends AbstractServerTest {

	private static final double DELTA = 0.001;

	private DocPriceImport resource;

	@Autowired
	private ProvResource provResource;

	@Autowired
	private ProvQuoteInstanceResource qiResource;

	@Autowired
	private ProvQuoteDatabaseResource qbResource;

	@Autowired
	private ProvQuoteStorageResource qsResource;

	@Autowired
	private ProvQuoteSupportResource qs2Resource;

	@Autowired
	private ProvQuoteRepository repository;

	@Autowired
	private ConfigurationResource configuration;

	protected int subscription;

	@BeforeEach
	void prepareData() throws IOException {
		persistSystemEntities();
		persistEntities("csv",
				new Class[] { Node.class, Project.class, CacheCompany.class, CacheUser.class, DelegateNode.class,
						Parameter.class, ProvLocation.class, Subscription.class, ParameterValue.class,
						ProvQuote.class },
				StandardCharsets.UTF_8.name());
		this.subscription = getSubscription("gStack");

		// Mock catalog import helper
		final var helper = new ImportCatalogResource();
		applicationContext.getAutowireCapableBeanFactory().autowireBean(helper);
		this.resource = initCatalog(helper, new DocPriceImport());

		clearAllCache();
		initSpringSecurityContext(DEFAULT_USER);
		resetImportTask();

		final var usage12 = new ProvUsage();
		usage12.setName("12month");
		usage12.setRate(100);
		usage12.setDuration(12);
		usage12.setConfiguration(repository.findBy("subscription.id", subscription));
		em.persist(usage12);

		final var usage36 = new ProvUsage();
		usage36.setName("36month");
		usage36.setRate(100);
		usage36.setDuration(36);
		usage36.setConfiguration(repository.findBy("subscription.id", subscription));
		em.persist(usage36);

		final var usageDev = new ProvUsage();
		usageDev.setName("dev");
		usageDev.setRate(30);
		usageDev.setDuration(1);
		usageDev.setConfiguration(repository.findBy("subscription.id", subscription));
		em.persist(usageDev);
		em.flush();
		em.clear();
	}

	private <T extends AbstractImportCatalogResource> T initCatalog(ImportCatalogResource importHelper, T catalog) {
		applicationContext.getAutowireCapableBeanFactory().autowireBean(catalog);
		catalog.setImportCatalogResource(importHelper);
		MethodUtils.getMethodsListWithAnnotation(catalog.getClass(), PostConstruct.class).forEach(m -> {
			try {
				m.invoke(catalog);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// Ignore;
			}
		});
		return catalog;
	}

	private void resetImportTask() {
		this.resource.getImportCatalogResource().endTask("service:prov:digitalocean", false);
		this.resource.getImportCatalogResource().startTask("service:prov:digitalocean", t -> {
			t.setLocation(null);
			t.setNbInstancePrices(null);
			t.setNbInstanceTypes(null);
			t.setNbStorageTypes(null);
			t.setWorkload(0);
			t.setDone(0);
			t.setPhase(null);
		});
	}

	@Test
	void installOffLineKoSizes() throws Exception {
		configuration.put(DocPriceImport.CONF_API_PRICES, "http://localhost:" + MOCK_PORT);
		httpServer.stubFor(get(urlEqualTo("/options_for_create.json")).willReturn(aResponse()
				.withStatus(HttpStatus.SC_OK)
				.withBody(IOUtils.toString(
						new ClassPathResource("mock-server/digitalocean/options_for_create.json").getInputStream(),
						"UTF-8"))));
		httpServer.stubFor(get(urlEqualTo("/aurora.js")).willReturn(aResponse().withStatus(HttpStatus.SC_OK)
				.withBody(IOUtils.toString(
						new ClassPathResource("mock-server/digitalocean/aurora-ko-sizes.js").getInputStream(),
						"UTF-8"))));
		httpServer.start();

		Assertions.assertThrows(BusinessException.class, () -> resource.install(false));
	}

	@Test
	void installOffLineKoPrices() throws Exception {
		configuration.put(DocPriceImport.CONF_API_PRICES, "http://localhost:" + MOCK_PORT);
		httpServer.stubFor(get(urlEqualTo("/options_for_create.json")).willReturn(aResponse()
				.withStatus(HttpStatus.SC_OK)
				.withBody(IOUtils.toString(
						new ClassPathResource("mock-server/digitalocean/options_for_create.json").getInputStream(),
						"UTF-8"))));
		httpServer.stubFor(get(urlEqualTo("/aurora.js")).willReturn(aResponse().withStatus(HttpStatus.SC_OK)
				.withBody(IOUtils.toString(
						new ClassPathResource("mock-server/digitalocean/aurora-ko-prices.js").getInputStream(),
						"UTF-8"))));
		httpServer.start();

		Assertions.assertThrows(BusinessException.class, () -> resource.install(false));
	}

	@Test
	void isEnabledRegionDatabaseDisabled() {
		final var context = new UpdateContext();
		context.setValidRegion(Pattern.compile("--"));
		Assertions.assertFalse(resource.isEnabledRegionDatabase(context, "any"));
	}

	@Test
	void isEnabledRegionDatabaseUnavailable() {
		final var context = new UpdateContext();
		context.setValidRegion(Pattern.compile("sf1"));
		context.setRegionsDatabase(Collections.singletonList("sf2"));
		Assertions.assertFalse(resource.isEnabledRegionDatabase(context, "sf1"));
	}

	@Test
	void isEnabledRegionDatabaseAvailable() {
		final var context = new UpdateContext();
		context.setValidRegion(Pattern.compile("sf1"));
		context.setRegionsDatabase(Collections.singletonList("sf1"));
		Assertions.assertTrue(resource.isEnabledRegionDatabase(context, "sf1"));
	}

	@Test
	void isEnabledRegionVolumeDisabled() {
		final var context = new UpdateContext();
		context.setValidRegion(Pattern.compile("--"));
		Assertions.assertFalse(resource.isEnabledRegionVolume(context, "any"));
	}

	@Test
	void isEnabledRegionVolumeUnavailable() {
		final var context = new UpdateContext();
		context.setValidRegion(Pattern.compile("sf1"));
		context.setRegionsVolume(Collections.singletonList("sf2"));
		Assertions.assertFalse(resource.isEnabledRegionVolume(context, "sf1"));
	}

	@Test
	void isEnabledRegionVolumeAvailable() {
		final var context = new UpdateContext();
		context.setValidRegion(Pattern.compile("sf1"));
		context.setRegionsVolume(Collections.singletonList("sf1"));
		Assertions.assertTrue(resource.isEnabledRegionVolume(context, "sf1"));
	}

	@Test
	void installOffLine() throws Exception {
		// Install a new configuration
		final var quote = install();

		// Check the whole quote
		final var instance = check(quote, 15d, 30d, 5d);

		// Check the 3 years term
		var lookup = qiResource.lookup(instance.getConfiguration().getSubscription().getId(),
				builder().cpu(7).ram(1741).constant(true).usage("36month").build());
		Assertions.assertEquals(2240d, lookup.getCost(), DELTA);
		Assertions.assertEquals(2240d, lookup.getPrice().getCost(), DELTA);
		Assertions.assertEquals(2240d, lookup.getPrice().getCostPeriod(), DELTA);
		Assertions.assertEquals("monthly", lookup.getPrice().getTerm().getCode());
		Assertions.assertFalse(lookup.getPrice().getTerm().isEphemeral());
		Assertions.assertEquals(1.0, lookup.getPrice().getPeriod(), DELTA);
		Assertions.assertEquals("nyc1/monthly/centos/m6-32vcpu-256gb", lookup.getPrice().getCode());
		Assertions.assertEquals("m6-32vcpu-256gb", lookup.getPrice().getType().getCode());
		Assertions.assertEquals("nyc1", lookup.getPrice().getLocation().getName());
		Assertions.assertEquals("New York 1", lookup.getPrice().getLocation().getDescription());
		checkImportStatus();

		// Check physical CPU
		// CPU Intensive
		lookup = qiResource.lookup(instance.getConfiguration().getSubscription().getId(),
				builder().cpu(2).ram(4096).constant(true).build());
		Assertions.assertEquals("nyc1/monthly/centos/c-2-4GiB", lookup.getPrice().getCode());
		Assertions.assertEquals("Intel Xeon", lookup.getPrice().getType().getProcessor());

		// General Purpose
		lookup = qiResource.lookup(instance.getConfiguration().getSubscription().getId(),
				builder().cpu(2).ram(8000).constant(true).build());
		Assertions.assertEquals("nyc1/monthly/centos/g-2vcpu-8gb", lookup.getPrice().getCode());
		Assertions.assertEquals("Intel Xeon Skylake", lookup.getPrice().getType().getProcessor());

		// Install again to check the update without change
		resetImportTask();
		resource.install(false);
		provResource.updateCost(subscription);
		check(provResource.getConfiguration(subscription), 15d, 30d, 5d);
		checkImportStatus();

		// Now, change a price within the remote catalog

		// Point to another catalog with different prices
		configuration.put(DocPriceImport.CONF_API_PRICES, "http://localhost:" + MOCK_PORT + "/v2");

		// Install the new catalog, update occurs
		resetImportTask();
		resource.install(false);
		provResource.updateCost(subscription);

		// Check the new price
		final var newQuote = provResource.getConfiguration(subscription);
		Assertions.assertEquals(16.0d, newQuote.getCost().getMin(), DELTA);

		// Compute price is updated
		final var instance2 = newQuote.getInstances().get(0);
		Assertions.assertEquals(6.0d, instance2.getCost(), DELTA);

		// Check status
		checkImportStatus();

		// Check the support
		Assertions.assertEquals(0, qs2Resource
				.lookup(subscription, 0, SupportType.ALL, SupportType.ALL, SupportType.ALL, SupportType.ALL, Rate.BEST)
				.size());

		final var lookupSu = qs2Resource
				.lookup(subscription, 0, null, SupportType.ALL, SupportType.ALL, SupportType.ALL, Rate.BEST).get(0);
		Assertions.assertEquals("Premier", lookupSu.getPrice().getType().getName());
		Assertions.assertEquals(5000.0d, lookupSu.getCost(), DELTA);

		// Check the database
		var lookupB = qbResource.lookup(subscription, QuoteDatabaseQuery.builder().cpu(1).engine("MYSQL").build());
		Assertions.assertNull(lookupB.getPrice().getEdition());
		Assertions.assertEquals("nyc1/monthly/db-1-1/MySQL", lookupB.getPrice().getCode());
		Assertions.assertEquals(1024, lookupB.getPrice().getType().getRam());
		Assertions.assertEquals(1, lookupB.getPrice().getType().getCpu());
		Assertions.assertNull(lookupB.getPrice().getStorageEngine());
	}

	private void checkImportStatus() {
		final var status = this.resource.getImportCatalogResource().getTask("service:prov:digitalocean");
		Assertions.assertEquals(6, status.getDone());
		Assertions.assertEquals(6, status.getWorkload());
		Assertions.assertEquals("install-support", status.getPhase());
		Assertions.assertEquals(DEFAULT_USER, status.getAuthor());
		Assertions.assertTrue(status.getNbInstancePrices().intValue() >= 372);
		Assertions.assertTrue(status.getNbInstanceTypes().intValue() >= 12);
		Assertions.assertTrue(status.getNbLocations() >= 1);
		Assertions.assertTrue(status.getNbStorageTypes().intValue() >= 3);
	}

	private void mockServer() throws IOException {
		configuration.put(DocPriceImport.CONF_API_PRICES, "http://localhost:" + MOCK_PORT);
		httpServer.stubFor(get(urlEqualTo("/options_for_create.json")).willReturn(aResponse()
				.withStatus(HttpStatus.SC_OK)
				.withBody(IOUtils.toString(
						new ClassPathResource("mock-server/digitalocean/options_for_create.json").getInputStream(),
						"UTF-8"))));
		httpServer.stubFor(get(urlEqualTo("/v2/options_for_create.json")).willReturn(aResponse()
				.withStatus(HttpStatus.SC_OK)
				.withBody(IOUtils.toString(
						new ClassPathResource("mock-server/digitalocean/v2/options_for_create.json").getInputStream(),
						"UTF-8"))));
		httpServer.stubFor(get(urlEqualTo("/aurora.js"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody(IOUtils.toString(
						new ClassPathResource("mock-server/digitalocean/aurora.js").getInputStream(), "UTF-8"))));
		httpServer.stubFor(get(urlEqualTo("/v2/aurora.js"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody(IOUtils.toString(
						new ClassPathResource("mock-server/digitalocean/v2/aurora.js").getInputStream(), "UTF-8"))));
		httpServer.start();
	}

	private ProvQuoteInstance check(final QuoteVo quote, final double minCost, final double maxCost,
			final double instanceCost) {
		Assertions.assertEquals(minCost, quote.getCost().getMin(), DELTA);
		Assertions.assertEquals(maxCost, quote.getCost().getMax(), DELTA);
		checkStorage(quote.getStorages().get(0));
		return checkInstance(quote.getInstances().get(0), instanceCost);
	}

	private ProvQuoteInstance checkInstance(final ProvQuoteInstance instance, final double cost) {
		Assertions.assertEquals(cost, instance.getCost(), DELTA);
		final var price = instance.getPrice();
		Assertions.assertEquals(0, price.getInitialCost());
		Assertions.assertEquals(VmOs.CENTOS, price.getOs());
		Assertions.assertEquals(ProvTenancy.SHARED, price.getTenancy());
		Assertions.assertEquals(5d, price.getCost(), DELTA);
		Assertions.assertEquals(5d, price.getCostPeriod(), DELTA);
		Assertions.assertEquals(1, price.getPeriod(), DELTA);
		final var term = price.getTerm();
		Assertions.assertEquals("monthly", term.getCode());
		Assertions.assertEquals("monthly", term.getName());
		Assertions.assertFalse(term.isEphemeral());
		Assertions.assertEquals(1, term.getPeriod());
		Assertions.assertEquals("s-1vcpu-1gb", price.getType().getCode());
		Assertions.assertEquals("s-1vcpu-1gb", price.getType().getName());
		Assertions.assertEquals("{Disk: 25, Category: Standard}", price.getType().getDescription());
		Assertions.assertNull(price.getType().getProcessor());
		Assertions.assertFalse(price.getType().isAutoScale());
		return instance;
	}

	private ProvQuoteStorage checkStorage(final ProvQuoteStorage storage) {
		Assertions.assertEquals(10d, storage.getCost(), DELTA);
		Assertions.assertEquals(100, storage.getSize(), DELTA);
		Assertions.assertNotNull(storage.getQuoteInstance());
		final var type = storage.getPrice().getType();
		Assertions.assertEquals("do-block-storage-standard", type.getCode());
		Assertions.assertEquals("do-block-storage-standard", type.getName());
		Assertions.assertEquals(5000, type.getIops());
		Assertions.assertEquals(200, type.getThroughput());
		Assertions.assertEquals(0d, storage.getPrice().getCostTransaction(), DELTA);
		Assertions.assertEquals(1, type.getMinimal());
		Assertions.assertEquals(16384, type.getMaximal().intValue());
		Assertions.assertEquals(Rate.GOOD, type.getLatency());
		Assertions.assertEquals(ProvStorageOptimized.IOPS, type.getOptimized());
		return storage;
	}

	/**
	 * Common offline install and configuring an instance
	 *
	 * @return The new quote from the installed
	 */
	private QuoteVo install() throws Exception {
		mockServer();

		// Check the basic quote
		return installAndConfigure();
	}

	@Test
	void installOnLine() throws Exception {
		configuration.delete(DocPriceImport.CONF_API_PRICES);
		configuration.put(DocPriceImport.CONF_REGIONS, "(sfo1|sfo2|nyc1|sgp1)");
		configuration.put(DocPriceImport.CONF_ITYPE, "(m6-|s-).*");
		configuration.put(DocPriceImport.CONF_DTYPE, "(db-1|db-2).*");
		configuration.put(DocPriceImport.CONF_ENGINE, "(MYSQL)");
		configuration.put(DocPriceImport.CONF_OS, "(WINDOWS|LINUX|CENTOS)");

		final var quote = installAndConfigure();
		Assertions.assertTrue(quote.getCost().getMin() >= 15);
		final var lookup = qiResource.lookup(subscription,
				builder().cpu(8).ram(26000).constant(true).type("m6-32vcpu-256gb").usage("36month").build());

		Assertions.assertTrue(lookup.getCost() > 900d);
		final var instance2 = lookup.getPrice();
		Assertions.assertEquals("monthly", instance2.getTerm().getCode());
		Assertions.assertEquals("m6-32vcpu-256gb", instance2.getType().getCode());
		Assertions.assertEquals("nyc1/monthly/centos/m6-32vcpu-256gb", instance2.getCode());
	}

	/**
	 * Install and check
	 */
	private QuoteVo installAndConfigure() throws IOException, Exception {
		resource.install(false);
		em.flush();
		em.clear();
		Assertions.assertEquals(0, provResource.getConfiguration(subscription).getCost().getMin(), DELTA);

		// Request an instance for a specific OS
		var lookup = qiResource.lookup(subscription,
				builder().cpu(8).ram(256000).constant(true).os(VmOs.CENTOS).location("sfo2").usage("36month").build());
		Assertions.assertEquals("sfo2/monthly/centos/m6-32vcpu-256gb", lookup.getPrice().getCode());

		// Request an instance for a generic Linux OS
		lookup = qiResource.lookup(subscription,
				builder().constant(true).type("s-1vcpu-1gb").os(VmOs.LINUX).location("sfo2").usage("36month").build());
		Assertions.assertEquals("sfo2/monthly/centos/s-1vcpu-1gb", lookup.getPrice().getCode());
		Assertions.assertFalse(lookup.getPrice().getType().isAutoScale());

		// New instance for "s-1vcpu-1gb"
		var ivo = new QuoteInstanceEditionVo();
		ivo.setCpu(1d);
		ivo.setRam(1);
		ivo.setLocation("sfo2");
		ivo.setPrice(lookup.getPrice().getId());
		ivo.setName("server1");
		ivo.setMaxQuantity(2);
		ivo.setSubscription(subscription);
		var createInstance = qiResource.create(ivo);
		Assertions.assertTrue(createInstance.getTotal().getMin() > 1);
		Assertions.assertTrue(createInstance.getId() > 0);

		// Lookup block storage (volume) within a region different from the one of attached server -> no match
		// ---------------------------------
		Assertions.assertEquals(0,
				qsResource.lookup(subscription,
						QuoteStorageQuery.builder().size(5).location("sgp1").instance(createInstance.getId()).build())
						.size());

		// Lookup block storage (volume) unavailable within this location
		// ---------------------------------
		Assertions.assertEquals(0, qsResource.lookup(subscription,
				QuoteStorageQuery.builder().size(5).location("sfo1").optimized(ProvStorageOptimized.IOPS).build())
				.size());

		// Lookup STANDARD SSD storage within the same region than the attached server
		// ---------------------------------
		var sLookup = qsResource.lookup(subscription, QuoteStorageQuery.builder().size(5).latency(Rate.LOW)
				.location("sfo2").instance(createInstance.getId()).build()).get(0);
		Assertions.assertEquals(0.5, sLookup.getCost(), DELTA);
		var price = sLookup.getPrice();
		Assertions.assertEquals("sfo2/do-block-storage-standard", price.getCode());
		var type = price.getType();
		Assertions.assertEquals("do-block-storage-standard", type.getCode());
		Assertions.assertEquals("sfo2", price.getLocation().getName());
		Assertions.assertEquals("San Francisco 2", price.getLocation().getDescription());

		// New storage attached to the created instance
		var svo = new QuoteStorageEditionVo();
		svo.setSize(100);
		svo.setName("storage1");
		svo.setSubscription(subscription);
		svo.setQuoteInstance(createInstance.getId());
		svo.setType(sLookup.getPrice().getType().getCode());
		var createStorage = qsResource.create(svo);
		Assertions.assertTrue(createStorage.getTotal().getMin() > 1);
		Assertions.assertTrue(createStorage.getId() > 0);

		// Lookup snapshot
		// ---------------------------------
		sLookup = qsResource.lookup(subscription, QuoteStorageQuery.builder().size(5).latency(Rate.LOW).location("sfo1")
				.optimized(ProvStorageOptimized.DURABILITY).build()).get(0);
		Assertions.assertEquals(0.25, sLookup.getCost(), DELTA);
		price = sLookup.getPrice();
		Assertions.assertEquals("sfo1/do-snapshot", price.getCode());
		type = price.getType();
		Assertions.assertEquals("do-snapshot", type.getCode());
		Assertions.assertEquals("sfo1", price.getLocation().getName());
		Assertions.assertEquals("San Francisco 1", price.getLocation().getDescription());
		Assertions.assertEquals("California", price.getLocation().getSubRegion());

		// Lookup Database unavailable in a region
		// ---------------------------------
		Assertions.assertNull(qbResource.lookup(subscription,
				QuoteDatabaseQuery.builder().location("sfo1").engine("MySQL").cpu(3).build()));

		// Lookup Database in an available region
		// ---------------------------------
		var dLookup = qbResource.lookup(subscription, QuoteDatabaseQuery.builder().engine("MySQL").cpu(2).build());
		Assertions.assertEquals(60, dLookup.getCost(), DELTA);
		var dPrice = dLookup.getPrice();
		Assertions.assertEquals("nyc1/monthly/db-2-4/MySQL", dPrice.getCode());
		var dType = dPrice.getType();
		Assertions.assertEquals("db-2-4", dType.getCode());
		Assertions.assertEquals("DB 2vCPU 4GiB", dType.getName());
		Assertions.assertEquals("nyc1", dPrice.getLocation().getName());
		Assertions.assertEquals("New York 1", dPrice.getLocation().getDescription());

		em.flush();
		em.clear();
		return provResource.getConfiguration(subscription);
	}

	/**
	 * Return the subscription identifier of the given project. Assumes there is only one subscription for a service.
	 */
	private int getSubscription(final String project) {
		return getSubscription(project, ProvDocPluginResource.KEY);
	}
}
