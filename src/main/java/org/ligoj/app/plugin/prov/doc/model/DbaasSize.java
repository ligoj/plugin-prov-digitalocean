package org.ligoj.app.plugin.prov.doc.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DbaasSize {
	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
	
	private int cpu;
	private double monthlyPrice;
	private double memory;
	private int disk;
}
