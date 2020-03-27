package org.ligoj.app.plugin.prov.doc.model;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Size {
	 private int id;
	 private String name;
	 @JsonProperty("cpu_count")
	 private double cpu;
	 @JsonProperty("price_per_hour")
	 private double pricePerHour;
	 @JsonProperty("price_per_month")
	 private double pricePerMonth;
	 @JsonProperty("disk_in_bytes")
	 private double diskInBytes;
	 @JsonProperty("memory_in_bytes")
	 private double memoryInBytes;
	 @JsonProperty("size_category")
	 private Category categorie;
	 List<Integer> regionIds = Collections.emptyList();

}
