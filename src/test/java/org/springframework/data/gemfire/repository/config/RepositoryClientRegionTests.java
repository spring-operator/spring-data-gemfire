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
package org.springframework.data.gemfire.repository.config;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.ForkUtil;
import org.springframework.data.gemfire.fork.SpringCacheServerProcess;
import org.springframework.data.gemfire.repository.sample.Person;
import org.springframework.data.gemfire.repository.sample.PersonRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author David Turanski
 * @author John Blum
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class RepositoryClientRegionTests {

	@Autowired
	PersonRepository repository;

	@BeforeClass
	public static void startUp() throws Exception {
		System.setProperty("gemfire.log-level", "warning");
		ForkUtil.startCacheServer(String.format("%1$s %2$s", SpringCacheServerProcess.class.getName(),
			"/org/springframework/data/gemfire/repository/config/RepositoryClientRegionTests-server-context.xml"));
	}

	@Test
	public void testFindAllAndCount() {
		assertEquals(2, repository.count());
		assertEquals(2, ((Collection<Person>) repository.findAll()).size());
	}

	@AfterClass
	public static void cleanUp() {
		ForkUtil.sendSignal();
	}
}
