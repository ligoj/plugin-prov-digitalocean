/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.app.plugin.prov.doc.catalog;

import java.util.List;

import org.ligoj.app.plugin.prov.catalog.AbstractUpdateContext;

import lombok.Getter;
import lombok.Setter;

/**
 * Context used to perform catalog update.
 */
@Getter
@Setter
public class UpdateContext extends AbstractUpdateContext {

	// Specific Context

	/**
	 * Database enabled regions.
	 * 
	 * @see <a href="https://www.digitalocean.com/docs/platform/availability-matrix/">DB availability</a>
	 */
	private List<String> regionsDatabase;
	
	/**
	 * Volume enabled regions.
	 * 
	 * @see <a href="https://www.digitalocean.com/docs/platform/availability-matrix/">DB availability</a>
	 */
	private List<String> regionsVolume;
}
