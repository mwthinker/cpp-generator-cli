project(${projectName}
	DESCRIPTION
		"Testing project using GTest"
	LANGUAGES
		CXX
)

find_package(GTest CONFIG)
if (GTest_FOUND)
	enable_testing()

	add_executable(${projectName}
		src/tests.cpp
		<#if extraFiles?has_content>
		${'\n'}
			<#list extraFiles as extraFile>
		${extraFile}
			</#list>
		</#if>
	)

	target_link_libraries(${projectName}
		PUBLIC
			GTest::gtest GTest::gtest_main # Test explorer on Visual Studio 2022 will not find test if "GTest::gmock_main GTest::gmock" is added?
	)

	if (MSVC)
		target_compile_options(${projectName}
			PRIVATE
				"/permissive-"
		)
	endif ()

	set_target_properties(${projectName}
		PROPERTIES
			CXX_STANDARD 20
			CXX_STANDARD_REQUIRED YES
			CXX_EXTENSIONS NO
	)
	
	include(GoogleTest)
	gtest_discover_tests(${projectName})

else ()
	message(STATUS "GTest not found, ${projectName} not created")
endif ()
