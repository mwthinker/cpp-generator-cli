cmake_minimum_required(VERSION 3.24)
project(${projectName}
	<#if description?has_content>
	DESCRIPTION
		"${description}"
	</#if>
	VERSION
		0.1.0
	LANGUAGES
		CXX
)

# Copy data to build folder.
file(COPY data/. DESTINATION ${r"${CMAKE_CURRENT_BINARY_DIR}"})

set(SOURCES
<#list sources as source>
	${source}
</#list>
<#if extraFiles?has_content>
	
	<#list extraFiles as extraFile>
	${extraFile}
	</#list>
</#if>
)

add_executable(${projectName}
	${r"${SOURCES}"}
)

set_property(GLOBAL PROPERTY USE_FOLDERS On)
set_property(DIRECTORY ${r"${CMAKE_CURRENT_SOURCE_DIR}"} PROPERTY VS_STARTUP_PROJECT ${projectName})
source_group(TREE
	${r"${CMAKE_CURRENT_SOURCE_DIR}"}
	FILES
		${r"${SOURCES}"}
)

<#if linkExternalLibraries?has_content>
set(ExternalDependencies
<#list linkExternalLibraries as lib>
	${lib.name()}
</#list>
)

include(ExternalFetchContent.cmake)
foreach(Dependency IN LISTS ExternalDependencies)
	find_package(${r"${Dependency}"} REQUIRED)
endforeach()
</#if>
<#if testProjectName?has_content>
add_subdirectory(${testProjectName})
</#if>

<#list vcpkgDependencies as dependency>
find_package(${dependency} CONFIG REQUIRED)
</#list>

<#if linkExternalLibraries?has_content>
set_target_properties(
	${r"${ExternalDependencies}"}

	PROPERTIES FOLDER
		ExternalDependencies
)
</#if>

if (MSVC)
	target_compile_options(${projectName}
		PUBLIC
			/W3 /WX /permissive-
			/MP
	)
else ()
	target_compile_options(${projectName}
		PRIVATE
			-Wall -pedantic -Wcast-align -Woverloaded-virtual -Wno-unused-parameter -Wno-sign-compare -Wno-unused-function
	)
endif ()

target_link_libraries(${projectName}
	PRIVATE
<#list linkLibraries as library>
		${library}
</#list>
)

set_target_properties(${projectName}
	PROPERTIES
		CXX_STANDARD 23
		CXX_STANDARD_REQUIRED YES
		CXX_EXTENSIONS NO
)
