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

rem Set Geck Driver if necessary
if DEFINED GECKO_DRIVER (
	set GECKO_PROPERTY=-Dwebdriver.gecko.driver=%GECKO_DRIVER%
) else if NOT %BROWSER:firefox=% == %BROWSER% (
	echo Missing GECKO_DRIVER variable. This script must be run from a run_client.bat script...
	exit 1
)
echo GECKO_PROPERTY=%GECKO_PROPERTY%

rem set maven command
set MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd
if NOT EXIST %MAVEN_CMD% set MAVEN_CMD=%MAVEN_HOME%\bin\mvn.bat
if NOT EXIST %MAVEN_CMD% (
	echo Cannot find Maven command %MAVEN_HOME%\bin\mvn.cmd or %MAVEN_HOME%\bin\mvn.bat
	exit 1
)

rem Compile framework core
cd %SPOT_CORE_DIR%
echo.
echo Compile framework core
call %MAVEN_CMD% -B -f spot-core\pom.xml install

rem Compile and run scenarios
echo.
echo Compile samples projects
call %MAVEN_CMD% -B -f spot-samples-pages\pom.xml install
call %MAVEN_CMD% -B -f spot-samples-scenarios\pom.xml install

echo Change directory to %SCENARIO_DIR%
cd %SCENARIO_DIR%
echo directory is now
cd

:LBL_FF78
rem Check whether scenario should run with Firefox 78esr
if NOT %BROWSER% == firefox_v78 if NOT %BROWSER% == all goto LBL_FF68
echo.
echo Run %SCENARIO_PREFIX%Scenario using Firefox 78esr browser...
call %MAVEN_CMD% -B -f pom.xml install -Dbrowser=firefox_v78 %GECKO_PROPERTY% %SCENARIO_ID% %ADDITIONNAL_ARG%

:LBL_FF68
rem Check whether scenario should run with Firefox 68esr
if NOT %BROWSER% == firefox_v68 if NOT %BROWSER% == all goto LBL_CHROME
echo.
echo Run %SCENARIO_PREFIX%Scenario using Firefox 68esr browser...
call %MAVEN_CMD% -B -f pom.xml install -Dbrowser=firefox_v68 %GECKO_PROPERTY% %SCENARIO_ID% %ADDITIONNAL_ARG%

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
	if NOT %BROWSER% == all goto LBL_FF60
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

:LBL_FF60
rem Check whether scenario should run with Firefox 60esr
if NOT %BROWSER% == firefox_v60 goto LBLFIN
echo.
echo Run %SCENARIO_PREFIX%Scenario using Firefox 60esr browser...
call %MAVEN_CMD% -B -f pom.xml install -Dbrowser=firefox_v60 %GECKO_PROPERTY% %SCENARIO_ID% %ADDITIONNAL_ARG%

:LBLFIN
