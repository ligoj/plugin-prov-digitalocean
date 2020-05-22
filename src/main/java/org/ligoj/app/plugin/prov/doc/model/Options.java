package org.ligoj.app.plugin.prov.doc.model;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Options {
	
	private List<Distribution> distributions = Collections.emptyList();
	private List<Size> sizes = Collections.emptyList();
	private List<Region> regions = Collections.emptyList();

}
