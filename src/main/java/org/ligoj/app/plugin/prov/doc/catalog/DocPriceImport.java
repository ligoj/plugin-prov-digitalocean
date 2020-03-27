/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.app.plugin.prov.doc.catalog;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.ligoj.app.plugin.prov.catalog.AbstractImportCatalogResource;
import org.ligoj.app.plugin.prov.doc.ProvDocPluginResource;
import org.ligoj.app.plugin.prov.doc.model.Image;
import org.ligoj.app.plugin.prov.doc.model.Options;
import org.ligoj.app.plugin.prov.doc.model.Size;
import org.ligoj.app.plugin.prov.model.ProvDatabasePrice;
import org.ligoj.app.plugin.prov.model.ProvDatabaseType;
import org.ligoj.app.plugin.prov.model.ProvInstancePrice;
import org.ligoj.app.plugin.prov.model.ProvInstancePriceTerm;
import org.ligoj.app.plugin.prov.model.ProvInstanceType;
import org.ligoj.app.plugin.prov.model.ProvLocation;
import org.ligoj.app.plugin.prov.model.ProvStorageOptimized;
import org.ligoj.app.plugin.prov.model.ProvStoragePrice;
import org.ligoj.app.plugin.prov.model.ProvStorageType;
import org.ligoj.app.plugin.prov.model.ProvSupportPrice;
import org.ligoj.app.plugin.prov.model.ProvSupportType;
import org.ligoj.app.plugin.prov.model.ProvTenancy;
import org.ligoj.app.plugin.prov.model.Rate;
import org.ligoj.app.plugin.prov.model.VmOs;
import org.ligoj.bootstrap.core.INamableBean;
import org.ligoj.bootstrap.core.curl.CurlProcessor;
import org.springframework.stereotype.Component;

import lombok.Setter;

/**
 * The provisioning price service for Digital Ocean. Manage install or update of prices.<br>
 */
@Component
@Setter
public class DocPriceImport extends AbstractImportCatalogResource {

	/**
	 * Configuration key used for URL prices.
	 */
	protected static final String CONF_API_PRICES = ProvDocPluginResource.KEY + ":prices-url";

	/**
	 * Configuration key used for enabled regions pattern names. When value is <code>null</code>, no restriction.
	 */
	protected static final String CONF_REGIONS = ProvDocPluginResource.KEY + ":regions";

	/**
	 * Default pricing URL.
	 */
	protected static final String DEFAULT_API_PRICES = "https://ligoj.io/plugins/plugin-prov-digitalocean";

	/**
	 * Name space for local configuration files
	 */
	protected static final String PREFIX = "digitalocean";

	/**
	 * Configuration key used for enabled instance type pattern names. When value is <code>null</code>, no restriction.
	 */
	public static final String CONF_ITYPE = ProvDocPluginResource.KEY + ":instance-type";

	/**
	 * Configuration key used for enabled database type pattern names. When value is <code>null</code>, no restriction.
	 */
	public static final String CONF_DTYPE = ProvDocPluginResource.KEY + ":database-type";
	/**
	 * Configuration key used for enabled database engine pattern names. When value is <code>null</code>, no
	 * restriction.
	 */
	public static final String CONF_ENGINE = ProvDocPluginResource.KEY + ":database-engine";

	/**
	 * Configuration key used for enabled OS pattern names. When value is <code>null</code>, no restriction.
	 */
	public static final String CONF_OS = ProvDocPluginResource.KEY + ":os";

	/**
	 * Configuration key used for enabled database engine pattern names. When value is <code>null</code>, no
	 * restriction.
	 */
	public static final String CONF_ETYPE = ProvDocPluginResource.KEY + ":database-engine";

	private String getPricesApi() {
		return configuration.get(CONF_API_PRICES, DEFAULT_API_PRICES);
	}

	/**
	 * Install or update prices.
	 *
	 * @param force When <code>true</code>, all cost attributes are update.
	 * @throws IOException When CSV or XML files cannot be read.
	 */
	public void install(final boolean force) throws IOException {
		final UpdateContext context = initContext(new UpdateContext(), ProvDocPluginResource.KEY, force);
		final var node = context.getNode();

		// Get previous data
		nextStep(node, "initialize");
		context.setValidOs(Pattern.compile(configuration.get(CONF_OS, ".*"), Pattern.CASE_INSENSITIVE));
		context.setValidDatabaseType(Pattern.compile(configuration.get(CONF_DTYPE, ".*"), Pattern.CASE_INSENSITIVE));
		context.setValidDatabaseEngine(Pattern.compile(configuration.get(CONF_ETYPE, ".*"), Pattern.CASE_INSENSITIVE));
		context.setValidInstanceType(Pattern.compile(configuration.get(CONF_ITYPE, ".*"), Pattern.CASE_INSENSITIVE));
		context.setValidRegion(Pattern.compile(configuration.get(CONF_REGIONS, ".*")));
		context.getMapRegionToName().putAll(toMap("regions.json", MAP_LOCATION));
		context.setInstanceTypes(itRepository.findAllBy(BY_NODE, node).stream()
				.collect(Collectors.toMap(ProvInstanceType::getCode, Function.identity())));
		context.setPrevious(ipRepository.findAllBy("term.node", node).stream()
				.collect(Collectors.toMap(ProvInstancePrice::getCode, Function.identity())));
		context.setPriceTerms(iptRepository.findAllBy(BY_NODE, node).stream()
				.collect(Collectors.toMap(ProvInstancePriceTerm::getCode, Function.identity())));
		context.setStorageTypes(stRepository.findAllBy(BY_NODE, node).stream()
				.collect(Collectors.toMap(ProvStorageType::getCode, Function.identity())));
		context.setPreviousStorage(spRepository.findAllBy("type.node", node).stream()
				.collect(Collectors.toMap(ProvStoragePrice::getCode, Function.identity())));
		context.setSupportTypes(st2Repository.findAllBy(BY_NODE, node).stream()
				.collect(Collectors.toMap(ProvSupportType::getName, Function.identity())));
		context.setPreviousSupport(sp2Repository.findAllBy("type.node", node).stream()
				.collect(Collectors.toMap(ProvSupportPrice::getCode, Function.identity())));
		context.setRegions(locationRepository.findAllBy(BY_NODE, context.getNode()).stream()
				.filter(r -> isEnabledRegion(context, r))
				.collect(Collectors.toMap(INamableBean::getName, Function.identity())));

		// Fetch the remote prices stream and build the prices object
		nextStep(node, "retrieve-catalog");

		// Instance(VM)

		var monthlyTerm = installPriceTerm(context, "monthly", 1);
		var hourlyTerm = installPriceTerm(context, "hourly", 0);

		try (var curl = new CurlProcessor()) {
			final var rawJson = StringUtils.defaultString(curl.get(getPricesApi() + "/options_for_create.json"), "{}");
			final var options = objectMapper.readValue(rawJson, Options.class);
			final var regionIds = new HashMap<Integer, ProvLocation>();

			// For each price/region/OS/software
			// Install term, type and price
			nextStep(node, "install-vm");
			options.getRegions().stream().filter(r -> isEnabledRegion(context, r.getSlug()))
					.forEach(r -> regionIds.put(r.getId(), installRegion(context, r.getSlug(), r.getName())));
			options.getSizes().stream().filter(s -> isEnabledType(context, s.getName()))
					.forEach(s -> s.setType(installInstanceType(context, s.getName(), s)));

			options.getDistributions().stream().filter(d -> isEnabledOs(context, getOs(d.getName()))).forEach(d -> {
				final var os = getOs(d.getName());
				getRegionsUnion(d.getImages()).stream().map(regionIds::get)
						.forEach(r -> options.getSizes().forEach(s -> {
							installInstancePrice(context, monthlyTerm, os, s.getType(), s.getPricePerMonth(), r);
							installInstancePrice(context, hourlyTerm, getOs(d.getName()), s.getType(),
									s.getPricePerHour() * context.getHoursMonth(), r);
						}));
			});

			nextStep(node, "install-vm-storage");
			var codeType = "-type-";
			var priceCode = "-priceCode-";
			var region = new ProvLocation();
			var sType = installStorageType(context, codeType, new Object());
			// installStoragePrice(context, priceCode, sType, 123d, region);
		}

		// Database
		nextStep(node, "install-database");
		try (var curl = new CurlProcessor()) {
			final var rawJS = StringUtils.defaultString(curl.get(getPricesApi() + "/aurora.js"), "");
			// For each price/region/engine
			// Install term, type and price
			var engine = "MYSQL";
			var codeType = "-type-";
			var region = "-france-";
			var termCode = "-term-";
			var priceCode = "-price-code-";
			var byol = false;
			String edition = null;
			String storageEngine = null;
			if (!isEnabledEngine(context, engine) || !isEnabledType(context, codeType)) {
				// Ignored type
				var type = installDatabaseType(context, codeType, new Object());
				var term = context.getPriceTerms().get(hourlyTerm.getCode());
				installDatabasePrice(context, term, priceCode, type, 123d, engine, edition, storageEngine, byol,
						region);
			}

			// Storage
			// Install type and price
			nextStep(node, "install-database-storage");
			var sType = installStorageType(context, codeType, new Object());
			installStoragePrice(context, priceCode, sType, 123d, region);
		}

		// Support
		// Install type and price
		nextStep(node, "install-support");
		var priceCode = "-price-code-";
		csvForBean.toBean(ProvSupportType.class, PREFIX + "/prov-support-type.csv").forEach(t -> {
			installSupportType(context, t.getName(), t);
		});
		csvForBean.toBean(ProvSupportPrice.class, PREFIX + "/prov-support-price.csv").forEach(t -> {
			installSupportPrice(context, priceCode, t);
		});

		// TODO

	}

	private VmOs getOs(final String osName) {
		return EnumUtils.getEnum(VmOs.class, osName.replace("redhat", "RHEL").replace("sles", "SUSE").toUpperCase());
	}

	private Set<Integer> getRegionsUnion(final List<Image> images) {
		return images.stream().map(image -> image.getRegionIds()).flatMap(List::stream).collect(Collectors.toSet());
	}

	/**
	 * Install or update a storage type.
	 */
	private ProvStorageType installStorageType(final UpdateContext context, final String code, final Object aType) {
		final var type = context.getStorageTypes().computeIfAbsent(code, c -> {
			final var newType = new ProvStorageType();
			newType.setNode(context.getNode());
			newType.setCode(c);
			return newType;
		});

		return copyAsNeeded(context, type, t -> {
			t.setName(code /* human readable name */);
			t.setLatency(Rate.MEDIUM);
			t.setAvailability(99d);
			t.setMinimal(1);
			t.setIncrement(null);
			t.setMaximal(1024d);
			t.setOptimized(ProvStorageOptimized.IOPS);
			t.setIops(10);
			t.setThroughput(20);
			t.setInstanceType("%");
			t.setDatabaseType("%");
		}, stRepository);
	}

	/**
	 * Install or update a storage price.
	 */
	private void installStoragePrice(final UpdateContext context, final String region, final ProvStorageType type,
			final double cost, final String code) {
		final var price = context.getPreviousStorage().computeIfAbsent(code, c -> {
			final var newPrice = new ProvStoragePrice();
			newPrice.setType(type);
			newPrice.setCode(c);
			return newPrice;
		});

		copyAsNeeded(context, price, p -> {
			p.setLocation(installRegion(context, region));
			p.setType(type);
		});

		// Update the cost
		saveAsNeeded(context, price, cost, spRepository);
	}

	/**
	 * Install a new instance price as needed.
	 */
	private void installInstancePrice(final UpdateContext context, final ProvInstancePriceTerm term, final VmOs os,
			final ProvInstanceType type, final double monthlyCost, final ProvLocation region) {
		final var price = context.getPrevious().computeIfAbsent(
				region.getName() + "/" + term.getCode() + "/" + type.getCode() + "/" + os.name(), code -> {
					// New instance price (not update mode)
					final var newPrice = new ProvInstancePrice();
					newPrice.setCode(code);
					return newPrice;
				});
		copyAsNeeded(context, price, p -> {
			p.setLocation(region);
			p.setOs(os);
			p.setTerm(term);
			p.setTenancy(ProvTenancy.SHARED);
			p.setType(type);
			p.setPeriod(term.getPeriod());
		});

		// Update the cost
		saveAsNeeded(context, price, monthlyCost, ipRepository);
	}

	/**
	 * Install a new instance type as needed.
	 */
	private ProvInstanceType installInstanceType(final UpdateContext context, final String code, final Size aType) {
		final var type = context.getInstanceTypes().computeIfAbsent(code, c -> {
			// New instance type (not update mode)
			final var newType = new ProvInstanceType();
			newType.setNode(context.getNode());
			newType.setCode(c);
			return newType;
		});

		// Merge as needed
		return copyAsNeeded(context, type, t -> {
			t.setName(code);
			t.setCpu(aType.getCpu());
			t.setRam((int) Math.ceil(aType.getMemoryInBytes() / 1024 / 1024 / 1024));
			t.setDescription("{Disk: " + aType.getDisk() + ", Category: " + aType.getCategorie().getName() + "}");
			t.setConstant(true);
			t.setAutoScale(false);

			// Rating
			t.setCpuRate(Rate.MEDIUM);
			t.setRamRate(Rate.MEDIUM);
			t.setNetworkRate(Rate.MEDIUM);
			t.setStorageRate(Rate.MEDIUM);
		}, itRepository);
	}

	/**
	 * Install a new price term as needed and complete the specifications.
	 */
	protected ProvInstancePriceTerm installPriceTerm(final UpdateContext context, final String code, final int period) {
		final var term = context.getPriceTerms().computeIfAbsent(code, t -> {
			final var newTerm = new ProvInstancePriceTerm();
			newTerm.setNode(context.getNode());
			newTerm.setCode(t);
			return newTerm;
		});

		// Complete the specifications
		return copyAsNeeded(context, term, t -> {
			t.setName(code /* human readable name */);
			t.setPeriod(period);
			t.setReservation(false);
			t.setConvertibleFamily(false);
			t.setConvertibleType(false);
			t.setConvertibleLocation(false);
			t.setConvertibleOs(false);
			t.setEphemeral(false);
		}, iptRepository);
	}

	/**
	 * Install a new database type as needed.
	 */
	private ProvDatabaseType installDatabaseType(final UpdateContext context, final String code, final Object aType) {
		final var type = context.getDatabaseTypes().computeIfAbsent(code, c -> {
			final var newType = new ProvDatabaseType();
			newType.setNode(context.getNode());
			newType.setCode(c);
			return newType;
		});

		// Merge as needed
		return copyAsNeeded(context, type, t -> {
			t.setName(code /* human readable name */);
			t.setCpu(2d);
			t.setRam((int) 2 * 1024);
			t.setDescription("-description'");
			t.setConstant(true);
			t.setAutoScale(false);

			// Rating
			t.setCpuRate(Rate.MEDIUM);
			t.setRamRate(Rate.MEDIUM);
			t.setNetworkRate(Rate.MEDIUM);
			t.setStorageRate(Rate.MEDIUM);
		}, dtRepository);
	}

	/**
	 * Install a new instance price as needed.
	 */
	private void installDatabasePrice(final UpdateContext context, final ProvInstancePriceTerm term,
			final String localCode, final ProvDatabaseType type, final double monthlyCost, final String engine,
			final String edition, final String storageEngine, final boolean byol, final String region) {
		final var price = context.getPreviousDatabase().computeIfAbsent(region + "/" + localCode, c -> {
			// New instance price
			final var newPrice = new ProvDatabasePrice();
			newPrice.setCode(c);
			return newPrice;
		});

		copyAsNeeded(context, price, p -> {
			p.setLocation(installRegion(context, region));
			p.setEngine(engine);
			p.setEdition(edition);
			p.setStorageEngine(storageEngine);
			p.setLicense(null /* ProvInstancePrice.LICENSE_BYOL */);
			p.setTerm(term);
			p.setType(type);
			p.setPeriod(term.getPeriod());
		});

		// Update the cost
		saveAsNeeded(context, price, round3Decimals(monthlyCost), dpRepository);
	}

	public void installSupportPrice(final UpdateContext context, final String code, final ProvSupportPrice aPrice) {
		final var price = context.getPreviousSupport().computeIfAbsent(code, c -> {
			// New instance price
			final ProvSupportPrice newPrice = new ProvSupportPrice();
			newPrice.setCode(c);
			return newPrice;
		});

		// Merge the support type details
		copyAsNeeded(context, price, p -> {
			p.setLimit(aPrice.getLimit());
			p.setMin(aPrice.getMin());
			p.setRate(aPrice.getRate());
			p.setType(aPrice.getType());
		});

		// Update the cost
		saveAsNeeded(context, price, price.getCost(), aPrice.getCost(), (cR, c) -> price.setCost(cR),
				sp2Repository::save);
	}

	private ProvSupportType installSupportType(final UpdateContext context, final String code,
			final ProvSupportType aType) {
		final var type = context.getSupportTypes().computeIfAbsent(code, c -> {
			var newType = new ProvSupportType();
			newType.setName(c);
			newType.setNode(context.getNode());
			return newType;
		});

		// Merge the support type details
		type.setDescription(aType.getDescription());
		type.setAccessApi(aType.getAccessApi());
		type.setAccessChat(aType.getAccessChat());
		type.setAccessEmail(aType.getAccessEmail());
		type.setAccessPhone(aType.getAccessPhone());
		type.setSlaStartTime(aType.getSlaStartTime());
		type.setSlaEndTime(aType.getSlaEndTime());
		type.setDescription(aType.getDescription());

		type.setSlaBusinessCriticalSystemDown(aType.getSlaBusinessCriticalSystemDown());
		type.setSlaGeneralGuidance(aType.getSlaGeneralGuidance());
		type.setSlaProductionSystemDown(aType.getSlaProductionSystemDown());
		type.setSlaProductionSystemImpaired(aType.getSlaProductionSystemImpaired());
		type.setSlaSystemImpaired(aType.getSlaSystemImpaired());
		type.setSlaWeekEnd(aType.isSlaWeekEnd());

		type.setCommitment(aType.getCommitment());
		type.setSeats(aType.getSeats());
		type.setLevel(aType.getLevel());
		st2Repository.save(type);
		return type;
	}

	/**
	 * Install a new region as needed.<br>
	 *
	 * @param context The current import context.
	 * @param region  The region code to install as needed.
	 * @param name    The region human name.
	 * @return The previous or the new installed region.
	 */
	private ProvLocation installRegion(final UpdateContext context, final String region, final String name) {
		final ProvLocation entity = context.getRegions().computeIfAbsent(region, r -> {
			final ProvLocation newRegion = new ProvLocation();
			newRegion.setNode(context.getNode());
			newRegion.setName(region);
			return newRegion;
		});

		// Update the location details as needed
		return copyAsNeeded(context, entity, r -> {
			final ProvLocation regionStats = context.getMapRegionToName().getOrDefault(r, new ProvLocation());
			r.setContinentM49(regionStats.getContinentM49());
			r.setCountryM49(regionStats.getCountryM49());
			r.setCountryA2(regionStats.getCountryA2());
			r.setPlacement(regionStats.getPlacement());
			r.setRegionM49(regionStats.getRegionM49());
			r.setSubRegion(regionStats.getSubRegion());
			r.setLatitude(regionStats.getLatitude());
			r.setLongitude(regionStats.getLongitude());
			r.setDescription(name);
		}, locationRepository);
	}
}
