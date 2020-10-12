@echo off
setlocal
set SCRIPT_DIR=%~dp0
echo Script dir is %SCRIPT_DIR%
set SCENARIO_DIR=%SCRIPT_DIR%..
echo Scenario dir is %SCENARIO_DIR%

rem Read arguments
call  %SCRIPT_DIR%read_arguments.bat %*

rem Write topology properties file
rem Nothing to do so far as we're currently using a static URL...
set TOPOLOGY_PROPERTIES_FILE_PATH=%SCENARIO_DIR%\params\topology.properties
echo Topology properties file %TOPOLOGY_PROPERTIES_FILE_PATH%:
type %TOPOLOGY_PROPERTIES_FILE_PATH%

rem Run scenarios
call %SCRIPT_DIR%run_scenario.bat