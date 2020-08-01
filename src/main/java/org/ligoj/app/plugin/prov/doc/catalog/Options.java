/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.app.plugin.prov.doc.catalog;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Distribution enabled: sizes in a set of regions.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Options {

	private List<Distribution> distributions = Collections.emptyList();
	private List<Price> sizes = Collections.emptyList();
	private List<Region> regions = Collections.emptyList();

}
