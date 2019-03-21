/*
 * Copyright 2010-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.gemfire.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * The AutoRegionLookupBeanDefinitionParser class is a Spring BeanDefinitionParser that registers a BeanPostProcessor
 * that auto registers Regions defined in GemFire's native cache.xml format, or when using GemFire 8's cluster-based
 * configuration service to define Regions, creating corresponding beans in the Spring context.
 *
 * @author John Blum
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see org.springframework.beans.factory.support.AbstractBeanDefinition
 * @see org.springframework.beans.factory.support.BeanDefinitionBuilder
 * @see org.springframework.beans.factory.xml.BeanDefinitionParser
 * @see org.springframework.beans.factory.xml.ParserContext
 * @see org.springframework.data.gemfire.config.AutoRegionLookupBeanPostProcessor
 * @see org.w3c.dom.Element
 * @since 1.5.0
 */
class AutoRegionLookupBeanDefinitionParser implements BeanDefinitionParser {

	@Override
	public BeanDefinition parse(final Element element, final ParserContext parserContext) {
		registerAutoRegionLookupBeanPostProcessor(element, parserContext);
		return null;
	}

	private void registerAutoRegionLookupBeanPostProcessor(final Element element, final ParserContext parserContext) {
		AbstractBeanDefinition autoRegionLookupBeanPostProcessor = BeanDefinitionBuilder
			.genericBeanDefinition(AutoRegionLookupBeanPostProcessor.class)
			.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
			.getBeanDefinition();

		autoRegionLookupBeanPostProcessor.setSource(parserContext.extractSource(element));

		BeanDefinitionReaderUtils.registerWithGeneratedName(autoRegionLookupBeanPostProcessor,
			parserContext.getRegistry());
	}

}
