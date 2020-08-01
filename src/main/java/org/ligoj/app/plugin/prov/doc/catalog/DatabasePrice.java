/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.app.plugin.prov.doc.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Database price holding the monthly price and the flat compute settings CPU and RAM.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabasePrice {

	private int cpu;
	private int monthlyPrice;
	private int memory;
}
