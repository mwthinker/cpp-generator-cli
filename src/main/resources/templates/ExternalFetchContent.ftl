include(FetchContent)

<#list externalProjects as project>
FetchContent_Declare(${project.name()}
	GIT_REPOSITORY
		${project.gitUrl()}
	GIT_TAG
		${project.gitTag()}
	OVERRIDE_FIND_PACKAGE
)
</#list>
