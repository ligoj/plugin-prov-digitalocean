package org.ligoj.app.plugin.prov.doc.model;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContainerDistribution {
	private float id;
	private String name;
	List<Image> images = Collections.emptyList();

}