/*
 * Copyright 2010-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.gemfire.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.util.StringUtils;

import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.CustomExpiry;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.EvictionAttributes;
import com.gemstone.gemfire.cache.ExpirationAttributes;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.compression.Compressor;

/**
 * The MockClientRegionFactory class...
 *
 * @author John Blum
 * @since 1.8.0
 */
@SuppressWarnings("unused")
public class MockClientRegionFactory<K, V> extends MockRegionFactory<K, V> {

	public MockClientRegionFactory(StubCache cache) {
		super(cache);
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public ClientRegionFactory<K, V> mockClientRegionFactory() {
		return mockClientRegionFactory(ClientRegionShortcut.PROXY);
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public ClientRegionFactory<K, V> mockClientRegionFactory(ClientRegionShortcut clientRegionShortcut) {
		attributesFactory = new com.gemstone.gemfire.cache.AttributesFactory<K,V>();
		attributesFactory.setDataPolicy(resolveDataPolicy(clientRegionShortcut));

		final ClientRegionFactory<K, V> mockClientRegionFactory = mock(ClientRegionFactory.class, "MockClientRegionFactory");

		when(mockClientRegionFactory.create(anyString())).thenAnswer(new Answer<Region>() {
			@Override public Region answer(InvocationOnMock invocation) throws Throwable {
				String name = (String) invocation.getArguments()[0];
				Region region = mockRegion(name);

				when(region.getAttributesMutator()).thenReturn(mockAttributesMutator);

				cache.allRegions().put(name, region);

				return region;
			}
		});

		when(mockClientRegionFactory.createSubregion(any(Region.class), anyString())).thenAnswer(new Answer<Region>() {
			@Override public Region answer(InvocationOnMock invocation) throws Throwable {
				Region parent = (Region) invocation.getArguments()[0];
				String name = (String) invocation.getArguments()[1];
				String parentRegionName = parent.getFullPath();

				assert parentRegionName != null : "Parent Region name was null!";

				String subRegionName = (parentRegionName.startsWith("/") ? parentRegionName+"/"+name
					: "/"+parentRegionName+"/"+ name);

				Region subRegion = mockRegion(subRegionName);

				cache.allRegions().put(subRegionName, subRegion);
				cache.allRegions().put(name, subRegion);

				return subRegion;
			}
		});

		when(mockClientRegionFactory.setCloningEnabled(anyBoolean())).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					attributesFactory.setCloningEnabled(Boolean.TRUE.equals(invocation.getArgumentAt(0, Boolean.class)));
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setCompressor(any(Compressor.class))).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					Compressor compressor = invocation.getArgumentAt(0, Compressor.class);
					attributesFactory.setCompressor(compressor);
					return mockClientRegionFactory;
				}
			}
		);

		doAnswer(new Answer<Void>() {
			@Override public Void answer(final InvocationOnMock invocation) throws Throwable {
			 	attributesFactory.setConcurrencyChecksEnabled(Boolean.TRUE.equals(
					invocation.getArgumentAt(0, Boolean.class)));
				return null;
			}
		}).when(mockClientRegionFactory).setConcurrencyChecksEnabled(anyBoolean());

		when(mockClientRegionFactory.setConcurrencyLevel(anyInt())).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					int concurrencyLevel = invocation.getArgumentAt(0, Integer.TYPE);
					attributesFactory.setConcurrencyLevel(concurrencyLevel);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setCustomEntryIdleTimeout(any(CustomExpiry.class))).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					CustomExpiry<K, V> customEntryIdleTimeout = invocation.getArgumentAt(0, CustomExpiry.class);
					attributesFactory.setCustomEntryIdleTimeout(customEntryIdleTimeout);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setCustomEntryTimeToLive(any(CustomExpiry.class))).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					CustomExpiry<K, V> customEntryTimeToLive = invocation.getArgumentAt(0, CustomExpiry.class);
					attributesFactory.setCustomEntryTimeToLive(customEntryTimeToLive);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setDiskStoreName(anyString())).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					String diskStoreName = invocation.getArgumentAt(0, String.class);
					attributesFactory.setDiskStoreName(diskStoreName);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setDiskSynchronous(anyBoolean())).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					attributesFactory.setDiskSynchronous(Boolean.TRUE.equals(
						invocation.getArgumentAt(0, Boolean.class)));
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setEntryIdleTimeout(any(ExpirationAttributes.class))).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					ExpirationAttributes entryIdleTimeout = invocation.getArgumentAt(0, ExpirationAttributes.class);
					attributesFactory.setEntryIdleTimeout(entryIdleTimeout);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setEntryTimeToLive(any(ExpirationAttributes.class))).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					ExpirationAttributes entryTimeToLive = invocation.getArgumentAt(0, ExpirationAttributes.class);
					attributesFactory.setEntryTimeToLive(entryTimeToLive);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setEvictionAttributes(any(EvictionAttributes.class))).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					EvictionAttributes evictionAttributes = invocation.getArgumentAt(0, EvictionAttributes.class);
					attributesFactory.setEvictionAttributes(evictionAttributes);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setInitialCapacity(anyInt())).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					int initialCapacity = invocation.getArgumentAt(0, Integer.TYPE);
					attributesFactory.setInitialCapacity(initialCapacity);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setKeyConstraint(any(Class.class))).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					Class keyConstraint = invocation.getArgumentAt(0, Class.class);
					attributesFactory.setKeyConstraint(keyConstraint);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setLoadFactor(anyFloat())).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					float loadFactor = invocation.getArgumentAt(0, Float.TYPE);
					attributesFactory.setLoadFactor(loadFactor);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setPoolName(anyString())).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					String poolName = invocation.getArgumentAt(0, String.class);
					poolName = (StringUtils.hasText(poolName) ? poolName : null);
					attributesFactory.setPoolName(poolName);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setRegionIdleTimeout(any(ExpirationAttributes.class))).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					ExpirationAttributes regionIdleTimeout = invocation.getArgumentAt(0, ExpirationAttributes.class);
					attributesFactory.setRegionIdleTimeout(regionIdleTimeout);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setRegionTimeToLive(any(ExpirationAttributes.class))).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					ExpirationAttributes regionTimeToLive = invocation.getArgumentAt(0, ExpirationAttributes.class);
					attributesFactory.setRegionTimeToLive(regionTimeToLive);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setStatisticsEnabled(anyBoolean())).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					attributesFactory.setStatisticsEnabled(Boolean.TRUE.equals(
						invocation.getArgumentAt(0, Boolean.class)));
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.setValueConstraint(any(Class.class))).thenAnswer(
			new Answer<ClientRegionFactory>() {
				@Override public ClientRegionFactory answer(final InvocationOnMock invocation) throws Throwable {
					Class valueConstraint = invocation.getArgumentAt(0, Class.class);
					attributesFactory.setValueConstraint(valueConstraint);
					return mockClientRegionFactory;
				}
			}
		);

		when(mockClientRegionFactory.addCacheListener(any(CacheListener.class))).thenAnswer(new Answer<ClientRegionFactory>(){
			@Override public ClientRegionFactory answer(InvocationOnMock invocation) throws Throwable {
				CacheListener cacheListener = (CacheListener) invocation.getArguments()[0];
				attributesFactory.addCacheListener(cacheListener);
				return mockClientRegionFactory;
			}
		});

		return mockClientRegionFactory;
	}

	ClientRegionFactory<K, V> createClientRegionFactory() {
		return mockClientRegionFactory();
	}

	ClientRegionFactory<K, V> createClientRegionFactory(ClientRegionShortcut shortcut) {
		return mockClientRegionFactory(shortcut);
	}

	DataPolicy resolveDataPolicy(ClientRegionShortcut clientRegionShortcut) {
		clientRegionShortcut = (clientRegionShortcut != null ? clientRegionShortcut : ClientRegionShortcut.LOCAL);

		switch (clientRegionShortcut) {
			case CACHING_PROXY:
			case CACHING_PROXY_HEAP_LRU:
			case CACHING_PROXY_OVERFLOW:
			case LOCAL:
			case LOCAL_HEAP_LRU:
			case LOCAL_OVERFLOW:
				return DataPolicy.NORMAL;
			case LOCAL_PERSISTENT:
			case LOCAL_PERSISTENT_OVERFLOW:
				return DataPolicy.PERSISTENT_REPLICATE;
			case PROXY:
				return DataPolicy.EMPTY;
			default:
				return DataPolicy.NORMAL;
		}
	}

}
