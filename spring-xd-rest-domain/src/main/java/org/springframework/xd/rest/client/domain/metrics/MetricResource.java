/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.rest.client.domain.metrics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceSupport;

/**
 * Base class for resources that adhere to the Metric interface.
 * 
 * @author Eric Bottard
 */
@XmlRootElement(name = "metric")
public class MetricResource extends ResourceSupport {

	/**
	 * The name of the represented metric.
	 */
	@XmlAttribute(name = "name")
	private String name;

	/**
	 * Construct a new resource.
	 */
	public MetricResource(String name) {
		this.name = name;
	}

	/**
	 * No arg constructor for serialization frameworks.
	 */
	protected MetricResource() {

	}

	/**
	 * Return the name of the represented metric. Unique per metric kind.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Dedicated subclass to workaround type erasure.
	 * 
	 * @author Eric Bottard
	 */
	@XmlRootElement(name = "metrics")
	public static class Page extends PagedResources<MetricResource> {

	}

}
