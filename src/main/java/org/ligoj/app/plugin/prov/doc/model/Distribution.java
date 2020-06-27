/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.app.plugin.prov.doc.model;

import java.util.Collections;
import java.util.List;

import org.ligoj.app.plugin.prov.doc.catalog.NamedBean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Enabled images for a distribution. A distribution is a named OS according to Digital Ocean naming. The
 * OS/distribution name is stored in {@link #getName()} property.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Distribution extends NamedBean {

	/**
	 * Default SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Available images for this OS/distribution.
	 */
	private List<Image> images = Collections.emptyList();

}
