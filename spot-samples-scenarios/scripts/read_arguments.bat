@echo off

rem Objective is to use this batch to read arguments when executing a run_client.bat scenario script

rem Set dirs
set SPOT_SCRIPTS_DIR=%~dp0
set SPOT_CORE_DIR=%SPOT_SCRIPTS_DIR%..\..
echo SPOT core root dir is %SPOT_CORE_DIR%

rem Read arguments
:LBLARGS
if %1_ == _ goto :LBLSTART & REM Jump when all parameters are consumed

if "%1" == "-b" (
	set BROWSER_ARG=%2
	shift
	shift
	goto LBLARGS
)
if "%1" == "-s" (
	set SCENARIO_ARG=%2
	shift
	shift
	goto LBLARGS
)
if "%1" == "-p" (
	set ADDITIONNAL_PARAMETERS_ARG=%2
	shift
	shift
	goto LBLARGS
)
if "%1" == "-a" (
	set ADDITIONNAL_ARGUMENTS_ARG=%ADDITIONNAL_ARGUMENTS_ARG% %2
	shift
	shift
	goto LBLARGS
)
if "%1" == "-h" (
	set HOSTNAME_ARG=%2
	shift
	shift
	goto LBLARGS
)
if "%1" == "-o" (
	set PORT_ARG=%2
	shift
	shift
	goto LBLARGS
)
if "%1" == "-t" (
	set TOPOLOGY_ARG=%2
	shift
	shift
	goto LBLARGS
)

rem echo Unknown argument '%1' will be ignored...
echo WARNING - '%1' is not a known argument, it will be ignored
shift
goto LBLARGS

:LBLSTART

rem Read browser argument
if "%BROWSER_ARG%" == "all" (
	set NEED_GECKO_DRIVER=true
) else if "%BROWSER_ARG%" == "firefox_v78" (
	set NEED_GECKO_DRIVER=true
) else if "%BROWSER_ARG%" == "firefox_v91" (
	set NEED_GECKO_DRIVER=true
) else if "%BROWSER_ARG%" == "firefox_v102" (
	set NEED_GECKO_DRIVER=true
) else if "%BROWSER_ARG%" == "firefox_v115" (
	set NEED_GECKO_DRIVER=true
) else if NOT "%BROWSER_ARG%" == "chrome" if NOT "%BROWSER_ARG%" == "edge" (
	echo %1 is not a valid browser type. Expecting either all, firefox_v115, firefox_v102, firefox_v91, firefox_v78, chrome or edge
	goto LBLERR
)
set BROWSER=%BROWSER_ARG%
echo SPOT scenarios will be run with %BROWSER% browser(s)

rem Check Gecko driver system property if specified
if "%NEED_GECKO_DRIVER%" == "true" (
	echo.
	if DEFINED GECKO_DRIVER (
		echo - Gecko driver location will be %GECKO_DRIVER%
		if NOT EXIST "%GECKO_DRIVER%" (
			echo Cannot find gecko driver file on local drive.
			goto LBLERR
		)
	) else (
		echo - Gecko driver path not defined by an environment variable, assuming it will be set using properties file.
	)
)

rem Scenario prefix and identifier
if NOT DEFINED SCENARIO_ARG (
	echo Missing scenario argument
	goto LBLERR
)
set SCENARIO_PREFIX=%SCENARIO_ARG%
echo SCENARIO_PREFIX=%SCENARIO_PREFIX%
set SCENARIO_ID=-Dscenario_identifier=%SCENARIO_PREFIX%
echo SCENARIO_ID=%SCENARIO_ID%
goto LBLFIN

:LBLERR
exit 1

:LBLFIN
