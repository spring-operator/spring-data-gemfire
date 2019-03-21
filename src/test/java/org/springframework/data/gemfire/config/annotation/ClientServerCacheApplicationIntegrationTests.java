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

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.LoaderHelper;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.process.ProcessWrapper;
import org.springframework.data.gemfire.test.support.ClientServerIntegrationTestsSupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test suite of test cases testing the contract and functionality of the {@link CacheServerApplication}
 * and {@link ClientCacheApplication} SDG annotation for configuring and bootstrapping the Pivotal GemFire
 * client/server topology
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.server.CacheServer
 * @since 1.9.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ClientServerCacheApplicationIntegrationTests.ClientCacheApplicationConfiguration.class)
@SuppressWarnings("all")
public class ClientServerCacheApplicationIntegrationTests extends ClientServerIntegrationTestsSupport {

	private static final int PORT = 12480;

	private static ProcessWrapper gemfireServerProcess;

	@BeforeClass
	public static void setupGemFireServer() throws Exception {
		gemfireServerProcess = run(CacheServerApplicationConfiguration.class, String.format("-Dgemfire.name=%1$s",
			asApplicationName(ClientServerCacheApplicationIntegrationTests.class)));

		waitForServerToStart("localhost", PORT);
	}

	@AfterClass
	public static void tearDownGemFireServer() {
		stop(gemfireServerProcess);
	}

	@Autowired
	private ClientCache clientCache;

	@Resource(name = "Echo")
	private Region<String, String> echo;

	@Test
	public void echoClientProxyRegionEchoesKeysForValues() {
		assertThat(echo.get("Hello")).isEqualTo("Hello");
		assertThat(echo.get("Test")).isEqualTo("Test");
	}

	@CacheServerApplication(name = "ClientServerCacheApplicationIntegrationTests", logLevel = "warn", port = PORT)
	public static class CacheServerApplicationConfiguration {

		public static void main(String[] args) {
			runSpringApplication(CacheServerApplicationConfiguration.class, args);
		}

		@Bean("Echo")
		public PartitionedRegionFactoryBean<String, String> echoRegion(Cache gemfireCache) {
			PartitionedRegionFactoryBean<String, String> echoRegion =
				new PartitionedRegionFactoryBean<String, String>();

			echoRegion.setCache(gemfireCache);
			echoRegion.setCacheLoader(echoCacheLoader());
			echoRegion.setClose(false);
			echoRegion.setPersistent(false);

			return echoRegion;
		}

		CacheLoader<String, String> echoCacheLoader() {
			return new CacheLoader<String, String>() {
				@Override
				public String load(LoaderHelper<String, String> helper) throws CacheLoaderException {
					return helper.getKey();
				}

				@Override
				public void close() {
				}
			};
		}
	}

	@ClientCacheApplication(logLevel = "warn", servers = { @ClientCacheApplication.Server(port = PORT)})
	static class ClientCacheApplicationConfiguration {

		@Bean(name = "Echo")
		ClientRegionFactoryBean<String, String> echoRegion(ClientCache gemfireCache) {
			ClientRegionFactoryBean<String, String> echoRegion = new ClientRegionFactoryBean<String, String>();

			echoRegion.setCache(gemfireCache);
			echoRegion.setClose(false);
			echoRegion.setShortcut(ClientRegionShortcut.PROXY);

			return echoRegion;
		}
	}
}
