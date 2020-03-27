/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.app.plugin.prov.doc.catalog;

import java.io.IOException;

import org.ligoj.app.plugin.prov.catalog.AbstractImportCatalogResource;
import org.springframework.stereotype.Component;

import lombok.Setter;

/**
 * The provisioning price service for Azure. Manage install or update of prices.<br>
 */
@Component
@Setter
public class DigitalOceanPriceImport extends AbstractImportCatalogResource {

	
	/**
	 * Install or update prices.
	 *
	 * @param force When <code>true</code>, all cost attributes are update.
	 * @throws IOException When CSV or XML files cannot be read.
	 */
	public void install(final boolean force) throws IOException {
		/*
		final UpdateContext context = initContext(new UpdateContext(), ProvAzurePluginResource.KEY, force);
		base.install(context);
		vm.install(context);
		database.install(context);
		disk.install(context);
		support.install(context);
		*/
	}
}
