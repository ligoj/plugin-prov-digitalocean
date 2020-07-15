/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.app.plugin.prov.doc.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Temporary extends of a not yet released NamedBean supporting globally "@JsonIgnoreProperties"
 * 
 * @deprecated
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated

public class NamedBean extends org.ligoj.bootstrap.core.NamedBean<Integer> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	// Only there for JSON failsafe
}