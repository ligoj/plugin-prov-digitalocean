package org.ligoj.app.plugin.prov.doc.model;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown=true)
public class Distribution {
	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	List<Image> images = Collections.emptyList();

}
