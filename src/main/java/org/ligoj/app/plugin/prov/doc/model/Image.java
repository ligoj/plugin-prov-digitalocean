/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.app.plugin.prov.doc.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * Hold the region identifiers and other unexploited data for now.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {

	@JsonProperty("region_ids")
	private List<Integer> regionIds;
}
