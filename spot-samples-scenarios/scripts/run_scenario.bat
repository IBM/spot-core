@echo off

rem Set OS type
set OS_TYPE=win

rem Check environment variables
if NOT DEFINED BROWSER (
	echo Missing BROWSER variable. This script must be run from a run_client.bat script...
	exit 1
)
echo BROWSER=%BROWSER%
if NOT DEFINED SCENARIO_DIR (
	echo Missing SCENARIO_DIR variable. This script must be run from a run_client.bat script...
	exit 1
)
echo SCENARIO_DIR=%SCENARIO_DIR%
if NOT DEFINED SCENARIO_ID (
	echo Missing SCENARIO_ID variable. This script must be run from a run_client.bat script...
	exit 1
)
echo SCENARIO_ID=%SCENARIO_ID%

rem Set additional parameters file if specified
if DEFINED ADDITIONNAL_PARAMETERS_ARG (
	if DEFINED ADDITIONNAL_ARG echo ADDITIONNAL_ARG=%ADDITIONNAL_ARG%
	echo ADDITIONNAL_PARAMETERS_ARG=%ADDITIONNAL_PARAMETERS_ARG%
	set ADDITIONNAL_ARG=%ADDITIONNAL_ARG% -Dspot.additional.params=scenarios/%ADDITIONNAL_PARAMETERS_ARG%.properties
)

rem Set additional arguments if specified
if DEFINED ADDITIONNAL_ARGUMENTS_ARG (
	if DEFINED ADDITIONNAL_ARG echo ADDITIONNAL_ARG=%ADDITIONNAL_ARG%
	echo ADDITIONNAL_ARGUMENTS_ARG=%ADDITIONNAL_ARGUMENTS_ARG%
	set ADDITIONNAL_ARG=%ADDITIONNAL_ARG% %ADDITIONNAL_ARGUMENTS_ARG%
)

rem Echo additional arg used
if DEFINED ADDITIONNAL_ARG (
	echo ADDITIONNAL_ARG=%ADDITIONNAL_ARG%
)

rem Set Gecko Driver if necessary
if DEFINED GECKO_DRIVER (
	set GECKO_PROPERTY=-Dwebdriver.gecko.driver=%GECKO_DRIVER%
	echo GECKO_PROPERTY=%GECKO_PROPERTY%
) 

rem set maven command
set MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd
if NOT EXIST %MAVEN_CMD% set MAVEN_CMD=%MAVEN_HOME%\bin\mvn.bat
if NOT EXIST %MAVEN_CMD% (
	echo Cannot find Maven command %MAVEN_HOME%\bin\mvn.cmd or %MAVEN_HOME%\bin\mvn.bat
	exit 1
)

rem Compile framework core and samples projects
cd %SPOT_CORE_DIR%
echo.
echo Compile framework core and samples projects
call %MAVEN_CMD% -B -f pom.xml install -DskipTests

echo Change directory to %SCENARIO_DIR%
cd %SCENARIO_DIR%
echo directory is now
cd

:LBL_FF115
rem Check whether scenario should run with Firefox 115esr
if NOT %BROWSER% == firefox_v115 if NOT %BROWSER% == all goto LBL_FF102
echo.
echo Run %SCENARIO_PREFIX%Scenario using Firefox 115esr browser...
call %MAVEN_CMD% -B -f pom.xml install -Dbrowser=firefox_v115 %GECKO_PROPERTY% %SCENARIO_ID% %ADDITIONNAL_ARG%

:LBL_FF102
rem Check whether scenario should run with Firefox 102esr
if NOT %BROWSER% == firefox_v102 if NOT %BROWSER% == all goto LBL_CHROME
echo.
echo Run %SCENARIO_PREFIX%Scenario using Firefox 102esr browser...
call %MAVEN_CMD% -B -f pom.xml install -Dbrowser=firefox_v102 %GECKO_PROPERTY% %SCENARIO_ID% %ADDITIONNAL_ARG%

:LBL_CHROME
rem Check whether scenario should run with Chrome
if NOT %BROWSER% == chrome if NOT %BROWSER% == all goto LBL_EDGE
echo.
echo Run %SCENARIO_PREFIX%Scenario using Chrome browser...
call %MAVEN_CMD% -B -f pom.xml install -Dbrowser=chrome %SCENARIO_ID% %ADDITIONNAL_ARG%

:LBL_EDGE
rem Check whether scenario should run with Edge
if %BROWSER% == edge (
	if NOT DEFINED EDGE_DRIVER (
		echo Missing EDGE_DRIVER environment variable
		echo Current VM does not seem to be correctly setup to run SPOT scenario with MS Edge
		exit 1
	)
	if NOT EXIST "%EDGE_DRIVER%" (
		echo MS Edge driver %EDGE_DRIVER% does not exist
		echo Current VM does not seem to be correctly setup to run SPOT scenario with MS Edge
		exit 2
	)
) else (
	if NOT %BROWSER% == all goto LBL_FF78
	if NOT DEFINED EDGE_DRIVER (
		echo EDGE_DRIVER environment variable is not defined
		echo Skip MS Edge scenario test for all browsers as current VM does not seem to be setup for it
		goto LBLFIN
	)
	if NOT EXIST "%EDGE_DRIVER%" (
		echo MS Edge driver %EDGE_DRIVER% does not exist
		echo Current VM does not seem to be correctly setup to run SPOT scenario with MS Edge
		exit 2
	)
)
echo.
echo Run %SCENARIO_PREFIX%Scenario using MS Edge browser...
call %MAVEN_CMD% -B -f pom.xml install -Dbrowser=edge %SCENARIO_ID% %ADDITIONNAL_ARG%
goto LBLFIN

rem =============================================================
rem Keep deprecated Firefox esr versions for ponctual unary tests
rem =============================================================

:LBL_FF78
rem Check whether scenario should run with Firefox 78esr
if NOT %BROWSER% == firefox_v78 goto LBL_FF91
echo.
echo Run %SCENARIO_PREFIX%Scenario using Firefox 78esr browser...
call %MAVEN_CMD% -B -f pom.xml install -Dbrowser=firefox_v78 %GECKO_PROPERTY% %SCENARIO_ID% %ADDITIONNAL_ARG%
goto LBLFIN

:LBL_FF91
rem Check whether scenario should run with Firefox 91esr
if NOT %BROWSER% == firefox_v91 goto LBLFIN
echo.
echo Run %SCENARIO_PREFIX%Scenario using Firefox 91esr browser...
call %MAVEN_CMD% -B -f pom.xml install -Dbrowser=firefox_v91 %GECKO_PROPERTY% %SCENARIO_ID% %ADDITIONNAL_ARG%
goto LBLFIN

:LBLFIN
