package org.ligoj.app.plugin.prov.doc.model;

import org.ligoj.bootstrap.core.model.AbstractNamedEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Region extends AbstractNamedEntity<Integer> {
		
	 /**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
	private String slug;
}