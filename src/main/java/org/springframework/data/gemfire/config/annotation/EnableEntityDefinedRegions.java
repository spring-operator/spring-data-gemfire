/*
 * Copyright 2016 the original author or authors.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.geode.cache.Region;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

/**
 * The {@link EnableEntityDefinedRegions} annotation marks a Spring {@link org.springframework.context.annotation.Configuration @Configuration}
 * annotated class to enable the creation of the GemFire/Geode {@link Region Regions} based on
 * the application domain model object entities.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.ComponentScan
 * @see org.springframework.context.annotation.ComponentScan.Filter
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.core.annotation.AliasFor
 * @see org.springframework.data.gemfire.config.annotation.EntityDefinedRegionsConfiguration
 * @see org.apache.geode.cache.Region
 * @since 1.9.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(IndexConfiguration.class)
@SuppressWarnings({ "unused" })
public @interface EnableEntityDefinedRegions {

	/**
	 * Alias for {@link #basePackages()} attribute.
	 *
	 * @return a {@link String} array specifying the packages to search for application persistent entities.
	 * @see #basePackages()
	 */
	@AliasFor(attribute = "basePackages")
	String[] value() default {};

	/**
	 * Base packages to scan for {@link org.springframework.data.gemfire.mapping.annotation.Region @Region} annotated
	 * application persistent entities.  {@link #value()} is an alias for this attribute.
	 * Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
	 *
	 * @return a {@link String} array specifying the packages to search for application persistent entities.
	 * @see #value()
	 */
	@AliasFor(attribute = "value")
	String[] basePackages() default {};

	/**
	 * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for
	 * {@link org.springframework.data.gemfire.mapping.annotation.Region @Region} annotated application persistent entities.
	 * The package of each class specified will be scanned.  Consider creating a special no-op marker class or interface
	 * in each package that serves no other purpose than being referenced by this attribute.
	 *
	 * @return an array of {@link Class classes} used to determine the packages to scan
	 * for application persistent entities.
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * Specifies which types are not eligible for component scanning.
	 *
	 * @return an array of {@link org.springframework.context.annotation.ComponentScan.Filter Filters} used to
	 * specify application persistent entities to be excluded during the component scan.
	 */
	ComponentScan.Filter[] excludeFilters() default {};

	/**
	 * Specifies which types are eligible for component scanning.  Further narrows the set of candidate components
	 * from everything in {@link #basePackages()} to everything in the base packages that matches the given filter
	 * or filters.
	 *
	 * @return an array {@link org.springframework.context.annotation.ComponentScan.Filter} of Filters used to
	 * specify application persistent entities to be included during the component scan.
	 */
	ComponentScan.Filter[] includeFilters() default {};

	/**
	 * Determines whether the created {@link Region} will have strongly-typed key and value constraints
	 * based on the ID and {@link Class} type of application persistent entity.
	 *
	 * Defaults to {@literal false}.
	 */
	boolean strict() default false;

}
