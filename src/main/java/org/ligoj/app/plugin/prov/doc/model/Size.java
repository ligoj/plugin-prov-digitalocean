package org.ligoj.app.plugin.prov.doc.model;

import java.util.Collections;
import java.util.List;

import org.ligoj.app.plugin.prov.model.ProvInstanceType;
import org.ligoj.bootstrap.core.NamedBean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Size extends NamedBean<Integer> {

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
	private NamedBean<Integer> categorie;
	List<Integer> regionIds = Collections.emptyList();

	/**
	 * Resolved instance type entity.
	 */
	@JsonIgnore
	private ProvInstanceType type;
}
