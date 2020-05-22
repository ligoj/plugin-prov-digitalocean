package org.ligoj.app.plugin.prov.doc.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DbaasSize {
	
	private int cpu;
	private int monthlyPrice;
	private int memory;
	private int disk;
}
