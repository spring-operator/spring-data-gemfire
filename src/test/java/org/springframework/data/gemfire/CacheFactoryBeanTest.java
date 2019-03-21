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

package org.springframework.data.gemfire;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheClosedException;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.CacheTransactionManager;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.TransactionListener;
import org.apache.geode.cache.TransactionWriter;
import org.apache.geode.cache.control.ResourceManager;
import org.apache.geode.cache.util.GatewayConflictResolver;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.distributed.DistributedSystem;
import org.apache.geode.distributed.Role;
import org.apache.geode.pdx.PdxSerializer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.gemfire.support.GemfireBeanFactoryLocator;
import org.springframework.data.util.ReflectionUtils;

/**
 * Unit tests for {@link CacheFactoryBean}.
 *
 * @author John Blum
 * @see org.junit.Rule
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see org.apache.geode.cache.Cache
 * @since 1.7.0
 */
public class CacheFactoryBeanTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void afterPropertiesSet() throws Exception {
		final AtomicBoolean postProcessBeforeCacheInitializationCalled = new AtomicBoolean(false);
		final Properties gemfireProperties = new Properties();

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean() {
			@Override protected void postProcessBeforeCacheInitialization(Properties actualGemfireProperties) {
				assertThat(actualGemfireProperties, is(sameInstance(gemfireProperties)));
				postProcessBeforeCacheInitializationCalled.set(true);
			}
		};

		cacheFactoryBean.setProperties(gemfireProperties);
		cacheFactoryBean.afterPropertiesSet();

		assertThat(postProcessBeforeCacheInitializationCalled.get(), is(true));
	}

	@Test
	public void postProcessBeforeCacheInitializationUsingDefaults() {
		Properties gemfireProperties = new Properties();

		new CacheFactoryBean().postProcessBeforeCacheInitialization(gemfireProperties);

		assertThat(gemfireProperties.size(), is(equalTo(2)));
		assertThat(gemfireProperties.containsKey("disable-auto-reconnect"), is(true));
		assertThat(gemfireProperties.containsKey("use-cluster-configuration"), is(true));
		assertThat(gemfireProperties.getProperty("disable-auto-reconnect"), is(equalTo("true")));
		assertThat(gemfireProperties.getProperty("use-cluster-configuration"), is(equalTo("false")));
	}

	@Test
	public void postProcessBeforeCacheInitializationWithAutoReconnectAndClusterConfigurationDisabled() {
		Properties gemfireProperties = new Properties();
		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setEnableAutoReconnect(false);
		cacheFactoryBean.setUseClusterConfiguration(false);
		cacheFactoryBean.postProcessBeforeCacheInitialization(gemfireProperties);

		assertThat(gemfireProperties.size(), is(equalTo(2)));
		assertThat(gemfireProperties.containsKey("disable-auto-reconnect"), is(true));
		assertThat(gemfireProperties.containsKey("use-cluster-configuration"), is(true));
		assertThat(gemfireProperties.getProperty("disable-auto-reconnect"), is(equalTo("true")));
		assertThat(gemfireProperties.getProperty("use-cluster-configuration"), is(equalTo("false")));
	}

	@Test
	public void postProcessBeforeCacheInitializationWithAutoReconnectAndClusterConfigurationEnabled() {
		Properties gemfireProperties = new Properties();
		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setEnableAutoReconnect(true);
		cacheFactoryBean.setUseClusterConfiguration(true);
		cacheFactoryBean.postProcessBeforeCacheInitialization(gemfireProperties);

		assertThat(gemfireProperties.size(), is(equalTo(2)));
		assertThat(gemfireProperties.containsKey("disable-auto-reconnect"), is(true));
		assertThat(gemfireProperties.containsKey("use-cluster-configuration"), is(true));
		assertThat(gemfireProperties.getProperty("disable-auto-reconnect"), is(equalTo("false")));
		assertThat(gemfireProperties.getProperty("use-cluster-configuration"), is(equalTo("true")));
	}

	@Test
	public void postProcessBeforeCacheInitializationWithAutoReconnectDisabledAndClusterConfigurationEnabled() {
		Properties gemfireProperties = new Properties();
		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setEnableAutoReconnect(false);
		cacheFactoryBean.setUseClusterConfiguration(true);
		cacheFactoryBean.postProcessBeforeCacheInitialization(gemfireProperties);

		assertThat(gemfireProperties.size(), is(equalTo(2)));
		assertThat(gemfireProperties.containsKey("disable-auto-reconnect"), is(true));
		assertThat(gemfireProperties.containsKey("use-cluster-configuration"), is(true));
		assertThat(gemfireProperties.getProperty("disable-auto-reconnect"), is(equalTo("true")));
		assertThat(gemfireProperties.getProperty("use-cluster-configuration"), is(equalTo("true")));
	}

	@Test
	public void getObjectCallsInit() throws Exception {
		final Cache mockCache = mock(Cache.class);

		final AtomicBoolean initCalled = new AtomicBoolean(false);

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean() {
			@Override Cache init() throws Exception {
				initCalled.set(true);
				return mockCache;
			}
		};

		assertThat(cacheFactoryBean.getObject(), is(sameInstance(mockCache)));
		assertThat(initCalled.get(), is(true));

		verifyZeroInteractions(mockCache);
	}

	@Test
	public void getObjectReturnsExistingCache() throws Exception {
		Cache mockCache = mock(Cache.class);
		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setCache(mockCache);

		assertThat(cacheFactoryBean.getObject(), is(sameInstance(mockCache)));

		verifyZeroInteractions(mockCache);
	}

	@Test
	public void init() throws Exception {
		BeanFactory mockBeanFactory = mock(BeanFactory.class);
		Cache mockCache = mock(Cache.class);
		CacheTransactionManager mockCacheTransactionManager = mock(CacheTransactionManager.class);
		DistributedMember mockDistributedMember = mock(DistributedMember.class);
		DistributedSystem mockDistributedSystem = mock(DistributedSystem.class);
		GatewayConflictResolver mockGatewayConflictResolver = mock(GatewayConflictResolver.class);
		PdxSerializer mockPdxSerializer = mock(PdxSerializer.class);
		Resource mockCacheXml = mock(Resource.class);
		ResourceManager mockResourceManager = mock(ResourceManager.class);
		TransactionListener mockTransactionLister = mock(TransactionListener.class);
		TransactionWriter mockTransactionWriter = mock(TransactionWriter.class);

		final CacheFactory mockCacheFactory = mock(CacheFactory.class);

		when(mockBeanFactory.getAliases(anyString())).thenReturn(new String[0]);
		when(mockCacheFactory.create()).thenReturn(mockCache);
		when(mockCache.getCacheTransactionManager()).thenReturn(mockCacheTransactionManager);
		when(mockCache.getDistributedSystem()).thenReturn(mockDistributedSystem);
		when(mockCache.getResourceManager()).thenReturn(mockResourceManager);
		when(mockCacheXml.getInputStream()).thenReturn(mock(InputStream.class));
		when(mockDistributedSystem.getDistributedMember()).thenReturn(mockDistributedMember);
		when(mockDistributedSystem.getName()).thenReturn("MockDistributedSystem");
		when(mockDistributedMember.getId()).thenReturn("MockDistributedMember");
		when(mockDistributedMember.getGroups()).thenReturn(Collections.<String>emptyList());
		when(mockDistributedMember.getRoles()).thenReturn(Collections.<Role>emptySet());
		when(mockDistributedMember.getHost()).thenReturn("skullbox");
		when(mockDistributedMember.getProcessId()).thenReturn(12345);

		final ClassLoader expectedThreadContextClassLoader = Thread.currentThread().getContextClassLoader();

		final Properties gemfireProperties = new Properties();

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean() {
			@Override protected Object createFactory(final Properties actualGemfireProperties) {
				assertThat(actualGemfireProperties, is(equalTo(gemfireProperties)));
				assertThat(getBeanClassLoader(), is(equalTo(ClassLoader.getSystemClassLoader())));
				return mockCacheFactory;
			}
		};

		cacheFactoryBean.setBeanClassLoader(ClassLoader.getSystemClassLoader());
		cacheFactoryBean.setBeanFactory(mockBeanFactory);
		cacheFactoryBean.setBeanName("TestGemFireCache");
		cacheFactoryBean.setCacheXml(mockCacheXml);
		cacheFactoryBean.setCopyOnRead(true);
		cacheFactoryBean.setCriticalHeapPercentage(0.90f);
		cacheFactoryBean.setDynamicRegionSupport(null);
		cacheFactoryBean.setEnableAutoReconnect(false);
		cacheFactoryBean.setEvictionHeapPercentage(0.75f);
		cacheFactoryBean.setGatewayConflictResolver(mockGatewayConflictResolver);
		cacheFactoryBean.setJndiDataSources(null);
		cacheFactoryBean.setLockLease(15000);
		cacheFactoryBean.setLockTimeout(5000);
		cacheFactoryBean.setMessageSyncInterval(20000);
		cacheFactoryBean.setPdxDiskStoreName("TestPdxDiskStore");
		cacheFactoryBean.setPdxIgnoreUnreadFields(false);
		cacheFactoryBean.setPdxPersistent(true);
		cacheFactoryBean.setPdxReadSerialized(true);
		cacheFactoryBean.setPdxSerializer(mockPdxSerializer);
		cacheFactoryBean.setProperties(gemfireProperties);
		cacheFactoryBean.setSearchTimeout(45000);
		cacheFactoryBean.setTransactionListeners(Collections.singletonList(mockTransactionLister));
		cacheFactoryBean.setTransactionWriter(mockTransactionWriter);
		cacheFactoryBean.setUseBeanFactoryLocator(true);

		cacheFactoryBean.afterPropertiesSet();
		cacheFactoryBean.init();

		assertThat(Thread.currentThread().getContextClassLoader(), is(sameInstance(expectedThreadContextClassLoader)));

		GemfireBeanFactoryLocator beanFactoryLocator = cacheFactoryBean.getBeanFactoryLocator();

		assertThat(beanFactoryLocator, is(notNullValue()));

		BeanFactory beanFactoryReference = beanFactoryLocator.useBeanFactory("TestGemFireCache");

		assertThat(beanFactoryReference, is(sameInstance(mockBeanFactory)));

		verify(mockBeanFactory, times(1)).getAliases(anyString());
		verify(mockCacheFactory, times(1)).setPdxDiskStore(eq("TestPdxDiskStore"));
		verify(mockCacheFactory, times(1)).setPdxIgnoreUnreadFields(eq(false));
		verify(mockCacheFactory, times(1)).setPdxPersistent(eq(true));
		verify(mockCacheFactory, times(1)).setPdxReadSerialized(eq(true));
		verify(mockCacheFactory, times(1)).setPdxSerializer(eq(mockPdxSerializer));
		verify(mockCacheFactory, times(1)).create();
		verify(mockCache, times(2)).getCacheTransactionManager();
		verify(mockCache, times(1)).loadCacheXml(any(InputStream.class));
		verify(mockCache, times(1)).setCopyOnRead(eq(true));
		verify(mockCache, times(1)).setGatewayConflictResolver(same(mockGatewayConflictResolver));
		verify(mockCache, times(1)).setLockLease(eq(15000));
		verify(mockCache, times(1)).setLockTimeout(eq(5000));
		verify(mockCache, times(1)).setMessageSyncInterval(eq(20000));
		verify(mockCache, times(2)).getResourceManager();
		verify(mockCache, times(1)).setSearchTimeout(eq(45000));
		verify(mockResourceManager, times(1)).setCriticalHeapPercentage(eq(0.90f));
		verify(mockResourceManager, times(1)).setEvictionHeapPercentage(eq(0.75f));
		verify(mockCacheTransactionManager, times(1)).addListener(same(mockTransactionLister));
		verify(mockCacheTransactionManager, times(1)).setWriter(same(mockTransactionWriter));
	}

	@Test
	public void resolveCacheCallsFetchCacheReturnsMock() {
		final Cache mockCache = mock(Cache.class);

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean() {
			@Override @SuppressWarnings("unchecked ")
			protected <T extends GemFireCache> T fetchCache() {
				return (T) mockCache;
			}
		};

		assertThat(cacheFactoryBean.resolveCache(), is(sameInstance(mockCache)));

		verifyZeroInteractions(mockCache);
	}

	@Test
	public void resolveCacheCreatesCacheWhenFetchCacheThrowsCacheClosedException() {
		final Cache mockCache = mock(Cache.class);
		final CacheFactory mockCacheFactory = mock(CacheFactory.class);

		when(mockCacheFactory.create()).thenReturn(mockCache);

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean() {
			@Override protected <T extends GemFireCache> T fetchCache() {
				throw new CacheClosedException("test");
			}

			@Override
			protected Object createFactory(final Properties gemfireProperties) {
				assertThat(gemfireProperties, is(sameInstance(getProperties())));
				return mockCacheFactory;
			}
		};

		assertThat(cacheFactoryBean.resolveCache(), is(equalTo(mockCache)));

		verify(mockCacheFactory, times(1)).create();
		verifyZeroInteractions(mockCache);
	}

	@Test
	public void fetchExistingCache() throws Exception {
		Cache mockCache = mock(Cache.class);
		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setCache(mockCache);

		Cache actualCache = cacheFactoryBean.fetchCache();

		assertThat(actualCache, is(sameInstance(mockCache)));

		verifyZeroInteractions(mockCache);
	}

	@Test
	public void resolveProperties() {
		Properties gemfireProperties = new Properties();
		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setProperties(gemfireProperties);

		assertThat(cacheFactoryBean.resolveProperties(), is(sameInstance(gemfireProperties)));
	}

	@Test
	public void resolvePropertiesWhenNull() {
		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setProperties(null);

		Properties gemfireProperties = cacheFactoryBean.resolveProperties();

		assertThat(gemfireProperties, is(notNullValue()));
		assertThat(gemfireProperties.isEmpty(), is(true));
	}

	@Test
	public void createFactory() {
		Properties gemfireProperties = new Properties();
		Object cacheFactoryReference = new CacheFactoryBean().createFactory(gemfireProperties);

		assertThat(cacheFactoryReference, is(instanceOf(CacheFactory.class)));
		assertThat(gemfireProperties.isEmpty(), is(true));

		CacheFactory cacheFactory = (CacheFactory) cacheFactoryReference;

		cacheFactory.set("name", "TestCreateCacheFactory");

		assertThat(gemfireProperties.containsKey("name"), is(true));
		assertThat(gemfireProperties.getProperty("name"), is(equalTo("TestCreateCacheFactory")));
	}

	@Test
	public void prepareFactoryWithUnspecifiedPdxOptions() {
		CacheFactory mockCacheFactory = mock(CacheFactory.class);

		assertThat((CacheFactory) new CacheFactoryBean().prepareFactory(mockCacheFactory),
			is(sameInstance(mockCacheFactory)));

		verify(mockCacheFactory, never()).setPdxDiskStore(any(String.class));
		verify(mockCacheFactory, never()).setPdxIgnoreUnreadFields(any(Boolean.class));
		verify(mockCacheFactory, never()).setPdxPersistent(any(Boolean.class));
		verify(mockCacheFactory, never()).setPdxReadSerialized(any(Boolean.class));
		verify(mockCacheFactory, never()).setPdxSerializer(any(PdxSerializer.class));
	}

	@Test
	public void prepareFactoryWithSpecificPdxOptions() {
		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setPdxSerializer(mock(PdxSerializer.class));
		cacheFactoryBean.setPdxReadSerialized(true);
		cacheFactoryBean.setPdxIgnoreUnreadFields(false);

		CacheFactory mockCacheFactory = mock(CacheFactory.class);

		assertThat((CacheFactory) cacheFactoryBean.prepareFactory(mockCacheFactory),
			is(sameInstance(mockCacheFactory)));

		verify(mockCacheFactory, never()).setPdxDiskStore(any(String.class));
		verify(mockCacheFactory, times(1)).setPdxIgnoreUnreadFields(eq(false));
		verify(mockCacheFactory, never()).setPdxPersistent(any(Boolean.class));
		verify(mockCacheFactory, times(1)).setPdxReadSerialized(eq(true));
		verify(mockCacheFactory, times(1)).setPdxSerializer(any(PdxSerializer.class));
	}

	@Test
	public void prepareFactoryWithAllPdxOptions() {
		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setPdxDiskStoreName("testPdxDiskStoreName");
		cacheFactoryBean.setPdxIgnoreUnreadFields(false);
		cacheFactoryBean.setPdxPersistent(true);
		cacheFactoryBean.setPdxReadSerialized(true);
		cacheFactoryBean.setPdxSerializer(mock(PdxSerializer.class));

		CacheFactory mockCacheFactory = mock(CacheFactory.class);

		assertThat((CacheFactory) cacheFactoryBean.prepareFactory(mockCacheFactory),
			is(sameInstance(mockCacheFactory)));

		verify(mockCacheFactory, times(1)).setPdxDiskStore(eq("testPdxDiskStoreName"));
		verify(mockCacheFactory, times(1)).setPdxIgnoreUnreadFields(eq(false));
		verify(mockCacheFactory, times(1)).setPdxPersistent(eq(true));
		verify(mockCacheFactory, times(1)).setPdxReadSerialized(eq(true));
		verify(mockCacheFactory, times(1)).setPdxSerializer(any(PdxSerializer.class));
	}

	@Test
	public void prepareFactoryWithInvalidTypeForPdxSerializer() {
		CacheFactory mockCacheFactory = mock(CacheFactory.class);

		Object pdxSerializer = new Object();

		try {
			CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

			cacheFactoryBean.setPdxSerializer(pdxSerializer);
			cacheFactoryBean.setPdxIgnoreUnreadFields(false);
			cacheFactoryBean.setPdxReadSerialized(true);

			exception.expect(IllegalArgumentException.class);
			exception.expectCause(is(nullValue(Throwable.class)));
			exception.expectMessage(containsString(String.format(
				"[%1$s] of type [java.lang.Object] is not a PdxSerializer", pdxSerializer)));

			cacheFactoryBean.prepareFactory(mockCacheFactory);
		}
		finally {
			verify(mockCacheFactory, never()).setPdxSerializer(any(PdxSerializer.class));
			verify(mockCacheFactory, never()).setPdxDiskStore(any(String.class));
			verify(mockCacheFactory, never()).setPdxIgnoreUnreadFields(any(Boolean.class));
			verify(mockCacheFactory, never()).setPdxPersistent(any(Boolean.class));
			verify(mockCacheFactory, never()).setPdxReadSerialized(any(Boolean.class));
		}
	}

	@Test
	public void createCacheWithExistingCache() throws Exception {
		Cache mockCache = mock(Cache.class);
		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setCache(mockCache);

		Cache actualCache = cacheFactoryBean.createCache(null);

		assertThat(actualCache, is(sameInstance(mockCache)));

		verifyZeroInteractions(mockCache);
	}

	@Test
	public void createCacheWithNoExistingCache() {
		Cache mockCache = mock(Cache.class);
		CacheFactory mockCacheFactory = mock(CacheFactory.class);

		when(mockCacheFactory.create()).thenReturn(mockCache);

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		Cache actualCache = cacheFactoryBean.createCache(mockCacheFactory);

		assertThat(actualCache, is(equalTo(mockCache)));

		verify(mockCacheFactory, times(1)).create();
		verifyZeroInteractions(mockCache);
	}

	@Test(expected = IllegalArgumentException.class)
	public void postProcessCacheWithInvalidCriticalHeapPercentage() throws Exception {
		try {
			CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

			cacheFactoryBean.setCriticalHeapPercentage(200.0f);
			cacheFactoryBean.postProcess(null);
		}
		catch (IllegalArgumentException expected) {
			assertEquals("'criticalHeapPercentage' (200.0) is invalid; must be > 0.0 and <= 100.0",
				expected.getMessage());
			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void postProcessCacheWithInvalidEvictionHeapPercentage() throws Exception {
		try {
			CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

			cacheFactoryBean.setEvictionHeapPercentage(-75.0f);
			cacheFactoryBean.postProcess(null);
		}
		catch (IllegalArgumentException expected) {
			assertEquals("'evictionHeapPercentage' (-75.0) is invalid; must be > 0.0 and <= 100.0",
				expected.getMessage());
			throw expected;
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getObjectType() {
		assertThat((Class<Cache>) new CacheFactoryBean().getObjectType(), is(equalTo(Cache.class)));
	}

	@Test
	public void getObjectTypeWithExistingCache() {
		Cache mockCache = mock(Cache.class);
		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setCache(mockCache);

		assertThat(cacheFactoryBean.getObjectType(), is(equalTo((Class) mockCache.getClass())));
	}

	@Test
	public void isSingleton() {
		assertTrue(new CacheFactoryBean().isSingleton());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void destroy() throws Exception {
		final AtomicBoolean fetchCacheCalled = new AtomicBoolean(false);
		final Cache mockCache = mock(Cache.class, "GemFireCache");

		GemfireBeanFactoryLocator mockGemfireBeanFactoryLocator = mock(GemfireBeanFactoryLocator.class);

		when(mockCache.isClosed()).thenReturn(false);

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean() {
			@Override protected GemFireCache fetchCache() {
				fetchCacheCalled.set(true);
				return mockCache;
			}
		};

		ReflectionUtils.setField(CacheFactoryBean.class.getDeclaredField("beanFactoryLocator"), cacheFactoryBean,
			mockGemfireBeanFactoryLocator);

		cacheFactoryBean.setClose(true);
		cacheFactoryBean.setUseBeanFactoryLocator(true);
		cacheFactoryBean.destroy();

		assertThat(fetchCacheCalled.get(), is(true));

		verify(mockCache, times(1)).isClosed();
		verify(mockCache, times(1)).close();
		verify(mockGemfireBeanFactoryLocator, times(1)).destroy();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void destroyWhenCacheIsNull() throws Exception {
		final AtomicBoolean fetchCacheCalled = new AtomicBoolean(false);

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean() {
			@Override protected <T extends GemFireCache> T fetchCache() {
				fetchCacheCalled.set(true);
				return null;
			}
		};

		cacheFactoryBean.setClose(true);
		cacheFactoryBean.setUseBeanFactoryLocator(true);
		cacheFactoryBean.destroy();

		assertTrue(fetchCacheCalled.get());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void destroyWhenCacheClosedIsTrue() throws Exception {
		final AtomicBoolean fetchCacheCalled = new AtomicBoolean(false);
		final Cache mockCache = mock(Cache.class, "GemFireCache");

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean() {
			@Override @SuppressWarnings("unchecked") protected <T extends GemFireCache> T fetchCache() {
				fetchCacheCalled.set(true);
				return (T) mockCache;
			}
		};

		cacheFactoryBean.setClose(false);
		cacheFactoryBean.setUseBeanFactoryLocator(false);
		cacheFactoryBean.destroy();

		verify(mockCache, never()).isClosed();
		verify(mockCache, never()).close();

		assertFalse(fetchCacheCalled.get());
	}

	@Test
	public void closeCache() {
		GemFireCache mockCache = mock(GemFireCache.class, "testCloseCache.MockCache");

		new CacheFactoryBean().close(mockCache);

		verify(mockCache, times(1)).close();
	}

	@Test
	public void setAndGetCacheFactoryBeanProperties() throws Exception {
		BeanFactory mockBeanFactory = mock(BeanFactory.class, "SpringBeanFactory");
		GatewayConflictResolver mockGatewayConflictResolver = mock(GatewayConflictResolver.class, "GemFireGatewayConflictResolver");
		PdxSerializer mockPdxSerializer = mock(PdxSerializer.class, "GemFirePdxSerializer");
		Resource mockCacheXml = mock(Resource.class, "GemFireCacheXml");
		TransactionListener mockTransactionListener = mock(TransactionListener.class, "GemFireTransactionListener");
		TransactionWriter mockTransactionWriter = mock(TransactionWriter.class, "GemFireTransactionWriter");

		Properties gemfireProperties = new Properties();

		CacheFactoryBean cacheFactoryBean = new CacheFactoryBean();

		cacheFactoryBean.setBeanClassLoader(Thread.currentThread().getContextClassLoader());
		cacheFactoryBean.setBeanFactory(mockBeanFactory);
		cacheFactoryBean.setBeanName("TestCache");
		cacheFactoryBean.setCacheXml(mockCacheXml);
		cacheFactoryBean.setProperties(gemfireProperties);
		cacheFactoryBean.setUseBeanFactoryLocator(false);
		cacheFactoryBean.setClose(false);
		cacheFactoryBean.setCopyOnRead(true);
		cacheFactoryBean.setDynamicRegionSupport(new CacheFactoryBean.DynamicRegionSupport());
		cacheFactoryBean.setEnableAutoReconnect(true);
		cacheFactoryBean.setCriticalHeapPercentage(0.95f);
		cacheFactoryBean.setEvictionHeapPercentage(0.70f);
		cacheFactoryBean.setGatewayConflictResolver(mockGatewayConflictResolver);
		cacheFactoryBean.setJndiDataSources(Collections.singletonList(new CacheFactoryBean.JndiDataSource()));
		cacheFactoryBean.setLockLease(15000);
		cacheFactoryBean.setLockTimeout(5000);
		cacheFactoryBean.setMessageSyncInterval(10000);
		cacheFactoryBean.setPdxSerializer(mockPdxSerializer);
		cacheFactoryBean.setPdxReadSerialized(false);
		cacheFactoryBean.setPdxPersistent(true);
		cacheFactoryBean.setPdxIgnoreUnreadFields(true);
		cacheFactoryBean.setPdxDiskStoreName("TestPdxDiskStore");
		cacheFactoryBean.setSearchTimeout(30000);
		cacheFactoryBean.setTransactionListeners(Collections.singletonList(mockTransactionListener));
		cacheFactoryBean.setTransactionWriter(mockTransactionWriter);
		cacheFactoryBean.setUseClusterConfiguration(true);

		assertEquals(Thread.currentThread().getContextClassLoader(), cacheFactoryBean.getBeanClassLoader());
		assertSame(mockBeanFactory, cacheFactoryBean.getBeanFactory());
		assertNull(cacheFactoryBean.getBeanFactoryLocator());
		assertEquals("TestCache", cacheFactoryBean.getBeanName());
		assertSame(mockCacheXml, cacheFactoryBean.getCacheXml());
		assertSame(gemfireProperties, cacheFactoryBean.getProperties());
		assertTrue(Boolean.FALSE.equals(TestUtils.readField("useBeanFactoryLocator", cacheFactoryBean)));
		assertTrue(Boolean.FALSE.equals(TestUtils.readField("close", cacheFactoryBean)));
		assertTrue(cacheFactoryBean.getCopyOnRead());
		assertEquals(0.95f, cacheFactoryBean.getCriticalHeapPercentage().floatValue(), 0.0f);
		assertNotNull(cacheFactoryBean.getDynamicRegionSupport());
		assertTrue(cacheFactoryBean.getEnableAutoReconnect());
		assertEquals(0.70f, cacheFactoryBean.getEvictionHeapPercentage().floatValue(), 0.0f);
		assertSame(mockGatewayConflictResolver, cacheFactoryBean.getGatewayConflictResolver());
		assertNotNull(cacheFactoryBean.getJndiDataSources());
		assertEquals(1, cacheFactoryBean.getJndiDataSources().size());
		assertEquals(15000, cacheFactoryBean.getLockLease().intValue());
		assertEquals(5000, cacheFactoryBean.getLockTimeout().intValue());
		assertEquals(10000, cacheFactoryBean.getMessageSyncInterval().intValue());
		assertSame(mockPdxSerializer, cacheFactoryBean.getPdxSerializer());
		assertFalse(cacheFactoryBean.getPdxReadSerialized());
		assertTrue(cacheFactoryBean.getPdxPersistent());
		assertTrue(cacheFactoryBean.getPdxIgnoreUnreadFields());
		assertEquals("TestPdxDiskStore", cacheFactoryBean.getPdxDiskStoreName());
		assertEquals(30000, cacheFactoryBean.getSearchTimeout().intValue());
		assertNotNull(cacheFactoryBean.getTransactionListeners());
		assertEquals(1, cacheFactoryBean.getTransactionListeners().size());
		assertSame(mockTransactionListener, cacheFactoryBean.getTransactionListeners().get(0));
		assertSame(mockTransactionWriter, cacheFactoryBean.getTransactionWriter());
		assertTrue(cacheFactoryBean.getUseClusterConfiguration());
	}
}
