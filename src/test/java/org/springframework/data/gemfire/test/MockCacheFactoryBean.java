/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.springframework.data.gemfire.test;

import org.apache.geode.cache.GemFireCache;
import org.springframework.data.gemfire.CacheFactoryBean;

/**
 * @author David Turanski
 * @author John Blum
 */
public class MockCacheFactoryBean extends CacheFactoryBean {

	public MockCacheFactoryBean() {
		this(null);
	}

	public MockCacheFactoryBean(CacheFactoryBean cacheFactoryBean) {
		setUseBeanFactoryLocator(false);

		if (cacheFactoryBean != null) {
			setBeanClassLoader(cacheFactoryBean.getBeanClassLoader());
			setBeanFactory(cacheFactoryBean.getBeanFactory());
			setBeanName(cacheFactoryBean.getBeanName());
			setCacheXml(cacheFactoryBean.getCacheXml());
			setPhase(cacheFactoryBean.getPhase());
			setProperties(cacheFactoryBean.getProperties());
			setClose(cacheFactoryBean.getClose());
			setCopyOnRead(cacheFactoryBean.getCopyOnRead());
			setCriticalHeapPercentage(cacheFactoryBean.getCriticalHeapPercentage());
			setDynamicRegionSupport(cacheFactoryBean.getDynamicRegionSupport());
			setEnableAutoReconnect(cacheFactoryBean.getEnableAutoReconnect());
			setEvictionHeapPercentage(cacheFactoryBean.getEvictionHeapPercentage());
			setGatewayConflictResolver(cacheFactoryBean.getGatewayConflictResolver());
			setJndiDataSources(cacheFactoryBean.getJndiDataSources());
			setLockLease(cacheFactoryBean.getLockLease());
			setLockTimeout(cacheFactoryBean.getLockTimeout());
			setMessageSyncInterval(cacheFactoryBean.getMessageSyncInterval());
			setPdxDiskStoreName(cacheFactoryBean.getPdxDiskStoreName());
			setPdxIgnoreUnreadFields(cacheFactoryBean.getPdxIgnoreUnreadFields());
			setPdxPersistent(cacheFactoryBean.getPdxPersistent());
			setPdxReadSerialized(cacheFactoryBean.getPdxReadSerialized());
			setPdxSerializer(cacheFactoryBean.getPdxSerializer());
			setSearchTimeout(cacheFactoryBean.getSearchTimeout());
			setTransactionListeners(cacheFactoryBean.getTransactionListeners());
			setTransactionWriter(cacheFactoryBean.getTransactionWriter());
			setUseBeanFactoryLocator(cacheFactoryBean.isUseBeanFactoryLocator());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T extends GemFireCache> T fetchCache() {
		StubCache stubCache = new StubCache();
		stubCache.setProperties(getProperties());
		return (T) stubCache;
	}

}
