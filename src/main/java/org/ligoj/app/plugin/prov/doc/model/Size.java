package org.ligoj.app.plugin.prov.doc.model;

import org.ligoj.app.plugin.prov.doc.catalog.NamedBean;
import org.ligoj.app.plugin.prov.model.ProvInstanceType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Size extends NamedBean {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("cpu_count")
	private double cpu;
	@JsonProperty("price_per_hour")
	private double pricePerHour;
	@JsonProperty("price_per_month")
	private double pricePerMonth;

	private int disk;
	@JsonProperty("memory_in_bytes")
	private double memoryInBytes;
	@JsonProperty("size_category")
	private NamedBean categorie;

	/**
	 * Resolved instance type entity.
	 */
	@JsonIgnore
	private ProvInstanceType type;
}
