package org.jire.swiftfp.build

import org.gradle.kotlin.dsl.DependencyHandlerScope

/**
 * @author Jire
 */

enum class Dependencies(
	val group: String,
	val module: String,
	val version: String
) {
	
	Netty("io.netty", "netty-all", "4.1.69.Final"),
	FastUtil("it.unimi.dsi", "fastutil", "8.5.6"),
	`SLF4J-Simple`("org.slf4j", "slf4j-simple", "1.7.32")
	
	;
	
	val notation = "$group:$module:$version"
	
	fun configure(
		scope: DependencyHandlerScope,
		configurationName: String = DEFAULT_CONFIGURATION_NAME
	) = scope.add(configurationName, notation)
	
	companion object {
		
		private const val DEFAULT_CONFIGURATION_NAME = "implementation"
		
		fun configure(
			scope: DependencyHandlerScope,
			configurationName: String = DEFAULT_CONFIGURATION_NAME,
			vararg dependencies: Dependencies = values()
		) = dependencies.forEach { it.configure(scope, configurationName) }
		
	}
	
}