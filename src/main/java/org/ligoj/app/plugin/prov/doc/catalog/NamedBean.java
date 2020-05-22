package org.ligoj.app.plugin.prov.doc.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NamedBean extends org.ligoj.bootstrap.core.NamedBean<Integer> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	// Only there for JSON failsafe
}
