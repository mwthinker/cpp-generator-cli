cmake_minimum_required(VERSION 3.24)
project(NewProject
	DESCRIPTION
		""
	VERSION
		0.1.0
	LANGUAGES
		CXX
)

# Copy data to build folder.
file(COPY data/. DESTINATION ${CMAKE_CURRENT_BINARY_DIR})

add_executable(NewProject
	src/main.cpp

	CMakePresets.json
	vcpkg.json
)

set_property(DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} PROPERTY VS_STARTUP_PROJECT NewProject)

find_package(fmt CONFIG REQUIRED)

if (MSVC)
	target_compile_options(NewProject
		PUBLIC
			/W3 /WX /permissive-
			/MP
	)
else ()
	target_compile_options(NewProject
		PRIVATE
			-Wall -pedantic -Wcast-align -Woverloaded-virtual -Wno-unused-parameter -Wno-sign-compare -Wno-unused-function
	)
endif ()

target_link_libraries(NewProject
	PRIVATE
		fmt::fmt-header-only
)

set_target_properties(NewProject
	PROPERTIES
		CXX_STANDARD 20
		CXX_STANDARD_REQUIRED YES
		CXX_EXTENSIONS NO
)
