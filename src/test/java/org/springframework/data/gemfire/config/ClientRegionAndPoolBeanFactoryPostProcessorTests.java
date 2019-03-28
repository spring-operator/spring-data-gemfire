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

package org.springframework.data.gemfire.config;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.matchers.VarargMatcher;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.client.PoolFactoryBean;

/**
 * The ClientRegionAndPoolBeanFactoryPostProcessorTests class is a test suite of test cases testing the contract
 * and functionality of the {@link ClientRegionAndPoolBeanFactoryPostProcessor} class.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.runners.MockitoJUnitRunner
 * @see org.springframework.data.gemfire.config.ClientRegionAndPoolBeanFactoryPostProcessor
 * @since 1.8.2
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientRegionAndPoolBeanFactoryPostProcessorTests {

	@Mock
	private BeanDefinition mockBeanDefinition;

	private ClientRegionAndPoolBeanFactoryPostProcessor beanFactoryPostProcessor =
		new ClientRegionAndPoolBeanFactoryPostProcessor();

	protected static <T> T[] asArray(T... array) {
		return array;
	}

	protected PropertyValue newPropertyValue(String name, Object value) {
		return new PropertyValue(name, value);
	}

	protected MutablePropertyValues newPropertyValues(PropertyValue... propertyValues) {
		return new MutablePropertyValues(Arrays.asList(propertyValues));
	}

	protected Matcher<String> stringArrayMatcher(String... expected) {
		return new ArrayMatcher<String>(expected);
	}

	@Test
	public void postProcessBeanFactory() {
		BeanDefinition mockClientRegionBeanOne = mock(BeanDefinition.class, "MockClientRegionBeanOne");
		BeanDefinition mockClientRegionBeanTwo = mock(BeanDefinition.class, "MockClientRegionBeanTwo");
		BeanDefinition mockPoolBean = mock(BeanDefinition.class, "MockPoolBean");

		ConfigurableListableBeanFactory mockBeanFactory = mock(ConfigurableListableBeanFactory.class, "MockBeanFactory");

		when(mockClientRegionBeanOne.getBeanClassName()).thenReturn(ClientRegionFactoryBean.class.getName());
		when(mockClientRegionBeanTwo.getBeanClassName()).thenReturn(ClientRegionFactoryBean.class.getName());
		when(mockPoolBean.getBeanClassName()).thenReturn(PoolFactoryBean.class.getName());
		when(mockBeanDefinition.getBeanClassName()).thenReturn(Object.class.getName());
		when(mockBeanFactory.getBeanDefinitionNames()).thenReturn(
			asArray("mockClientRegionBeanOne", "mockClientRegionBeanTwo", "mockGenericBean", "mockPoolBean"));
		when(mockBeanFactory.getBeanDefinition(eq("mockClientRegionBeanOne"))).thenReturn(mockClientRegionBeanOne);
		when(mockBeanFactory.getBeanDefinition(eq("mockClientRegionBeanTwo"))).thenReturn(mockClientRegionBeanTwo);
		when(mockBeanFactory.getBeanDefinition(eq("mockGenericBean"))).thenReturn(mockBeanDefinition);
		when(mockBeanFactory.getBeanDefinition(eq("mockPoolBean"))).thenReturn(mockPoolBean);
		when(mockClientRegionBeanOne.getDependsOn()).thenReturn(asArray("mockClientCacheBean"));
		when(mockClientRegionBeanOne.getPropertyValues()).thenReturn(newPropertyValues(newPropertyValue(
			ClientRegionAndPoolBeanFactoryPostProcessor.POOL_NAME_PROPERTY, "mockPoolBean")));
		when(mockClientRegionBeanTwo.getPropertyValues()).thenReturn(newPropertyValues(newPropertyValue(
			ClientRegionAndPoolBeanFactoryPostProcessor.POOL_NAME_PROPERTY, "DEFAULT")));

		beanFactoryPostProcessor.postProcessBeanFactory(mockBeanFactory);

		verify(mockBeanFactory, times(1)).getBeanDefinitionNames();
		verify(mockBeanFactory, times(2)).getBeanDefinition(eq("mockClientRegionBeanOne"));
		verify(mockBeanFactory, times(2)).getBeanDefinition(eq("mockClientRegionBeanTwo"));
		verify(mockBeanFactory, times(1)).getBeanDefinition(eq("mockGenericBean"));
		verify(mockBeanFactory, times(1)).getBeanDefinition(eq("mockPoolBean"));
		verify(mockBeanDefinition, times(2)).getBeanClassName();
		verify(mockClientRegionBeanOne, times(1)).getBeanClassName();
		verify(mockClientRegionBeanOne, times(1)).getPropertyValues();
		verify(mockClientRegionBeanOne, times(1)).getDependsOn();
		verify(mockClientRegionBeanOne, times(1)).setDependsOn(argThat(stringArrayMatcher(
			"mockClientCacheBean", "mockPoolBean")));
		verify(mockClientRegionBeanTwo, times(1)).getBeanClassName();
		verify(mockClientRegionBeanTwo, times(1)).getPropertyValues();
		verify(mockClientRegionBeanTwo, never()).getDependsOn();
		verify(mockClientRegionBeanTwo, never()).setDependsOn(isA(String[].class));
		verify(mockPoolBean, times(2)).getBeanClassName();
	}

	@Test
	public void isClientRegionBeanReturnsFalse() {
		when(mockBeanDefinition.getBeanClassName()).thenReturn(Object.class.getName());
		assertThat(beanFactoryPostProcessor.isClientRegionBean(mockBeanDefinition), is(false));
		verify(mockBeanDefinition, times(1)).getBeanClassName();
	}

	@Test
	public void isClientRegionBeanReturnsTrue() {
		when(mockBeanDefinition.getBeanClassName()).thenReturn(ClientRegionFactoryBean.class.getName());
		assertThat(beanFactoryPostProcessor.isClientRegionBean(mockBeanDefinition), is(true));
		verify(mockBeanDefinition, times(1)).getBeanClassName();
	}

	@Test
	public void isPoolBeanReturnsFalse() {
		when(mockBeanDefinition.getBeanClassName()).thenReturn(Object.class.getName());
		assertThat(beanFactoryPostProcessor.isPoolBean(mockBeanDefinition), is(false));
		verify(mockBeanDefinition, times(1)).getBeanClassName();
	}

	@Test
	public void isPoolBeanReturnsTrue() {
		when(mockBeanDefinition.getBeanClassName()).thenReturn(PoolFactoryBean.class.getName());
		assertThat(beanFactoryPostProcessor.isPoolBean(mockBeanDefinition), is(true));
		verify(mockBeanDefinition, times(1)).getBeanClassName();
	}

	@Test
	public void getPoolNameWhenPoolNamePropertyIsUnspecifiedReturnsNull() {
		when(mockBeanDefinition.getPropertyValues()).thenReturn(newPropertyValues());
		assertThat(beanFactoryPostProcessor.getPoolName(mockBeanDefinition), is(nullValue(String.class)));
		verify(mockBeanDefinition, times(1)).getPropertyValues();
	}

	@Test
	public void getPoolNameReturnsPoolBeanName() {
		when(mockBeanDefinition.getPropertyValues()).thenReturn(newPropertyValues(newPropertyValue(
			ClientRegionAndPoolBeanFactoryPostProcessor.POOL_NAME_PROPERTY, "testPoolName")));
		assertThat(beanFactoryPostProcessor.getPoolName(mockBeanDefinition), is(equalTo("testPoolName")));
		verify(mockBeanDefinition, times(1)).getPropertyValues();
	}

	@Test
	public void addDependsOnToExistingDependencies() {
		when(mockBeanDefinition.getDependsOn()).thenReturn(asArray("testBeanNameOne", "testBeanNameTwo"));
		assertThat(beanFactoryPostProcessor.addDependsOn(mockBeanDefinition, "testBeanNameThree"),
			is(sameInstance(mockBeanDefinition)));
		verify(mockBeanDefinition, times(1)).getDependsOn();
		verify(mockBeanDefinition, times(1)).setDependsOn(argThat(stringArrayMatcher(
			"testBeanNameOne", "testBeanNameTwo", "testBeanNameThree")));
	}

	@Test
	public void addDependsOnToNonExistingDependencies() {
		when(mockBeanDefinition.getDependsOn()).thenReturn(null);
		assertThat(beanFactoryPostProcessor.addDependsOn(mockBeanDefinition, "testBeanName"),
			is(sameInstance(mockBeanDefinition)));
		verify(mockBeanDefinition, times(1)).getDependsOn();
		verify(mockBeanDefinition, times(1)).setDependsOn(argThat(stringArrayMatcher("testBeanName")));
	}

	protected static final class ArrayMatcher<T> extends BaseMatcher<T> implements VarargMatcher {

		private final T[] expected;

		protected ArrayMatcher(T... expected) {
			this.expected = expected;
		}

		@Override
		public boolean matches(Object item) {
			Object[] actual = (item instanceof Object[] ? (Object[]) item : asArray(item));

			return Arrays.equals(actual, expected);
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(String.format("expected (%1$s)", this.expected));
		}
	}
}