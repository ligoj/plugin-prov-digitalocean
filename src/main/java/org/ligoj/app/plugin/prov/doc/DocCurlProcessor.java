/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.app.plugin.prov.doc;

import org.ligoj.bootstrap.core.curl.CurlProcessor;
import org.ligoj.bootstrap.core.curl.CurlRequest;

import lombok.Setter;

/**
 * Bearer Curl processor.
 */
public class DocCurlProcessor extends CurlProcessor {

	/**
	 * Token used to authenticate request
	 */
	@Setter
	protected String token;

	@Override
	protected boolean process(final CurlRequest request) {
		// Add headers for oAuth
		request.getHeaders().put("Authorization", "Bearer " + token);
		return super.process(request);
	}

}
