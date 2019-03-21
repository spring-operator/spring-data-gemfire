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
import java.util.Properties;

import org.springframework.data.gemfire.config.annotation.support.EmbeddedServiceConfigurationSupport;
import org.springframework.data.gemfire.util.PropertiesBuilder;

/**
 * The LoggingConfiguration class is a Spring {@link org.springframework.context.annotation.ImportBeanDefinitionRegistrar}
 * that applies additional GemFire/Geode configuration by way of GemFire/Geode System properties to configure
 * GemFire/Geode logging.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.config.annotation.EnableLogging
 * @see org.springframework.data.gemfire.config.annotation.support.EmbeddedServiceConfigurationSupport
 * @since 1.9.0
 */
public class LoggingConfiguration extends EmbeddedServiceConfigurationSupport {

	public static final int DEFAULT_LOG_DISK_SPACE_LIMIT = 0;
	public static final int DEFAULT_LOG_FILE_SIZE_LIMIT = 0;

	public static final String DEFAULT_LOG_LEVEL = "config";

	/* (non-Javadoc) */
	@Override
	protected Class getAnnotationType() {
		return EnableLogging.class;
	}

	/* (non-Javadoc) */
	@Override
	protected Properties toGemFireProperties(Map<String, Object> annotationAttributes) {
		PropertiesBuilder gemfireProperties = PropertiesBuilder.create();

		gemfireProperties.setPropertyIfNotDefault("log-disk-space-limit",
			annotationAttributes.get("logDiskSpaceLimit"), DEFAULT_LOG_DISK_SPACE_LIMIT);

		gemfireProperties.setProperty("log-file", annotationAttributes.get("logFile"));

		gemfireProperties.setPropertyIfNotDefault("log-file-size-limit",
			annotationAttributes.get("logFileSizeLimit"), DEFAULT_LOG_FILE_SIZE_LIMIT);

		gemfireProperties.setPropertyIfNotDefault("log-level",
			annotationAttributes.get("logLevel"), DEFAULT_LOG_LEVEL);

		return gemfireProperties.build();
	}
}
