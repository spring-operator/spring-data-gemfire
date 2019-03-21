/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.data.gemfire.config.annotation;

import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.gemfire.GemfireUtils;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.data.gemfire.support.ConnectionEndpointList;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The {@link AddCacheServerConfiguration} class is a Spring {@link ImportBeanDefinitionRegistrar} that registers
 * a {@link PoolFactoryBean} definition for the {@link org.apache.geode.cache.client.Pool}
 * configuration meta-data defined in {@link EnablePool}.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.ImportBeanDefinitionRegistrar
 * @see org.springframework.data.gemfire.config.annotation.EnablePool
 * @since 1.9.0
 */
public class AddPoolConfiguration implements ImportBeanDefinitionRegistrar {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		if (importingClassMetadata.hasAnnotation(EnablePool.class.getName())) {
			Map<String, Object> enablePoolAttributes = importingClassMetadata.getAnnotationAttributes(
				EnablePool.class.getName());

			registerPoolFactoryBeanDefinition(enablePoolAttributes, registry);
		}
	}

	/**
	 * Registers a {@link PoolFactoryBean} definition in the Spring application context configured with
	 * the {@link EnablePool} annotation meta-data.
	 *
	 * @param enablePoolAttributes {@link EnablePool} annotation attributes.
	 * @param registry Spring {@link BeanDefinitionRegistry used to register the {@link PoolFactoryBean} definition.
	 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry
	 * @see org.springframework.data.gemfire.client.PoolFactoryBean
	 * @see org.springframework.data.gemfire.config.annotation.EnablePool
	 * @see java.util.Map
	 */
	protected void registerPoolFactoryBeanDefinition(Map<String, Object> enablePoolAttributes,
			BeanDefinitionRegistry registry) {

		String poolName = getAndValidatePoolName(enablePoolAttributes);

		BeanDefinitionBuilder poolFactoryBean = BeanDefinitionBuilder.genericBeanDefinition(PoolFactoryBean.class);

		poolFactoryBean.addPropertyValue("freeConnectionTimeout", enablePoolAttributes.get("freeConnectionTimeout"));
		poolFactoryBean.addPropertyValue("idleTimeout", enablePoolAttributes.get("idleTimeout"));
		poolFactoryBean.addPropertyValue("loadConditioningInterval", enablePoolAttributes.get("loadConditioningInterval"));
		poolFactoryBean.addPropertyValue("maxConnections", enablePoolAttributes.get("maxConnections"));
		poolFactoryBean.addPropertyValue("minConnections", enablePoolAttributes.get("minConnections"));
		poolFactoryBean.addPropertyValue("multiUserAuthentication", enablePoolAttributes.get("multiUserAuthentication"));
		poolFactoryBean.addPropertyValue("pingInterval", enablePoolAttributes.get("pingInterval"));
		poolFactoryBean.addPropertyValue("prSingleHopEnabled", enablePoolAttributes.get("prSingleHopEnabled"));
		poolFactoryBean.addPropertyValue("readTimeout", enablePoolAttributes.get("readTimeout"));
		poolFactoryBean.addPropertyValue("retryAttempts", enablePoolAttributes.get("retryAttempts"));
		poolFactoryBean.addPropertyValue("serverGroup", enablePoolAttributes.get("serverGroup"));
		poolFactoryBean.addPropertyValue("socketBufferSize", enablePoolAttributes.get("socketBufferSize"));
		poolFactoryBean.addPropertyValue("statisticInterval", enablePoolAttributes.get("statisticInterval"));
		poolFactoryBean.addPropertyValue("subscriptionAckInterval", enablePoolAttributes.get("subscriptionAckInterval"));
		poolFactoryBean.addPropertyValue("subscriptionEnabled", enablePoolAttributes.get("subscriptionEnabled"));
		poolFactoryBean.addPropertyValue("subscriptionMessageTrackingTimeout", enablePoolAttributes.get("subscriptionMessageTrackingTimeout"));
		poolFactoryBean.addPropertyValue("subscriptionRedundancy", enablePoolAttributes.get("subscriptionRedundancy"));
		poolFactoryBean.addPropertyValue("threadLocalConnections", enablePoolAttributes.get("threadLocalConnections"));

		configurePoolConnections(enablePoolAttributes, poolFactoryBean);

		registry.registerBeanDefinition(poolName, poolFactoryBean.getBeanDefinition());
	}

	protected String getAndValidatePoolName(Map<String, Object> enablePoolAttributes) {
		String poolName = (String) enablePoolAttributes.get("name");
		Assert.hasText(poolName, "Pool name must be specified");
		return poolName;
	}

	/**
	 * Uses the list of GemFire Locator and Server connection endpoint definitions and meta-data to configure
	 * the GemFire client {@link org.apache.geode.cache.client.Pool} used to communicate with the servers
	 * in the GemFire cluster.
	 *
	 * @param enablePoolAttributes {@link EnablePool} annotation containing
	 * {@link org.apache.geode.cache.client.Pool} Locator/Server connection endpoint meta-data.
	 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
	 * @see java.util.Map
	 */
	protected BeanDefinitionBuilder configurePoolConnections(Map<String, Object> enablePoolAttributes,
			BeanDefinitionBuilder poolFactoryBean) {

		configureLocators(enablePoolAttributes, poolFactoryBean);
		configureServers(enablePoolAttributes, poolFactoryBean);

		return poolFactoryBean;
	}

	protected BeanDefinitionBuilder configureLocators(Map<String, Object> enablePoolAttributes,
			BeanDefinitionBuilder poolFactoryBean) {

		poolFactoryBean.addPropertyValue("locators", parseConnectionEndpoints(enablePoolAttributes,
			"locators", "locatorsString", GemfireUtils.DEFAULT_LOCATOR_PORT));

		return poolFactoryBean;
	}

	protected BeanDefinitionBuilder configureServers(Map<String, Object> enablePoolAttributes,
			BeanDefinitionBuilder poolFactoryBean) {

		poolFactoryBean.addPropertyValue("servers", parseConnectionEndpoints(enablePoolAttributes,
			"servers", "serversString", GemfireUtils.DEFAULT_CACHE_SERVER_PORT));

		return poolFactoryBean;
	}

	protected ConnectionEndpointList parseConnectionEndpoints(Map<String, Object> enablePoolAttributes,
			String arrayAttributeName, String stringAttributeName, int defaultPort) {

		ConnectionEndpointList connectionEndpoints = new ConnectionEndpointList();

		AnnotationAttributes[] connectionEndpointsMetaData =
			(AnnotationAttributes[]) enablePoolAttributes.get(arrayAttributeName);

		for (AnnotationAttributes annotationAttributes : connectionEndpointsMetaData) {
			connectionEndpoints.add(newConnectionEndpoint((String) annotationAttributes.get("host"),
				(Integer) annotationAttributes.get("port")));
		}

		String hostsPorts = (String) enablePoolAttributes.get(stringAttributeName);

		if (StringUtils.hasText(hostsPorts)) {
			connectionEndpoints.add(ConnectionEndpointList.parse(defaultPort, hostsPorts.split(",")));
		}

		return connectionEndpoints;
	}

	protected ConnectionEndpoint newConnectionEndpoint(String host, Integer port) {
		return new ConnectionEndpoint(host, port);
	}
}
