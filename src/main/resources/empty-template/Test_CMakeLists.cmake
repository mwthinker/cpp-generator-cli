project(NewProject
	DESCRIPTION
		"Testing project using GTest"
	LANGUAGES
		CXX
)

find_package(GTest CONFIG REQUIRED)
if (GTest_FOUND)
	enable_testing()

	add_executable(NewProject
		src/tests.cpp
	)

	target_link_libraries(NewProject
		PUBLIC
			GTest::gtest GTest::gtest_main # Test explorer on Visual Studio 2022 will not find test if "GTest::gmock_main GTest::gmock" is added?
	)

	if (MSVC)
		target_compile_options(NewProject
			PRIVATE
				"/permissive-"
		)
	endif ()

	set_target_properties(NewProject
		PROPERTIES
			CXX_STANDARD 20
			CXX_STANDARD_REQUIRED YES
			CXX_EXTENSIONS NO
	)
	
	include(GoogleTest)
	gtest_discover_tests(NewProject)

else ()
	message(STATUS "GTest not found, NewProject not created")
endif ()
