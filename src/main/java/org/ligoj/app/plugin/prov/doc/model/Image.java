package org.ligoj.app.plugin.prov.doc.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {

	@JsonProperty("region_ids")
	private List<Integer> regionIds;
}
