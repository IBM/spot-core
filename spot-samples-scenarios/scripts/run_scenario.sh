#!/bin/bash

# Set OS type
export OS_TYPE="linux"

# Check environment variables
if [[ -z ${BROWSER} ]]; then
	echo "Missing BROWSER variable. This script must be run from a run_client.sh script..."
	exit 1
fi
echo "BROWSER=${BROWSER}"
if [[ -z ${SCENARIO_DIR} ]]; then
	echo "Missing SCENARIO_DIR variable. This script must be run from a run_client.sh script..."
	exit 1
fi
echo "SCENARIO_DIR=${SCENARIO_DIR}"
if [[ -z ${SCENARIO_ID} ]]; then
	echo "Missing SCENARIO_ID variable. This script must be run from a run_client.sh script..."
	exit 1
fi
echo "SCENARIO_ID=${SCENARIO_ID}"

# Set additional parameters file if specified
if [[ -n ${ADDITIONNAL_PARAMETERS_ARG} ]]; then
	if [[ -n ${ADDITIONNAL_ARG} ]]; then echo "ADDITIONNAL_ARG=${ADDITIONNAL_ARG}"; fi
	echo "ADDITIONNAL_PARAMETERS_ARG=${ADDITIONNAL_PARAMETERS_ARG}"
	ADDITIONNAL_ARG="${ADDITIONNAL_ARG} -Dspot.additional.params=scenarios/${ADDITIONNAL_PARAMETERS_ARG}.properties"
fi

# Set additional arguments if specified
if [[ -n ${ADDITIONNAL_ARGUMENTS_ARG} ]]; then
	if [[ -n ${ADDITIONNAL_ARG} ]]; then echo "ADDITIONNAL_ARG=${ADDITIONNAL_ARG}"; fi
	echo "ADDITIONNAL_ARGUMENTS_ARG=${ADDITIONNAL_ARGUMENTS_ARG}"
	ADDITIONNAL_ARG="${ADDITIONNAL_ARG} ${ADDITIONNAL_ARGUMENTS_ARG}"
fi

# Echo additional arg used
if [[ -n ${ADDITIONNAL_ARG} ]]; then
	echo "ADDITIONNAL_ARG=${ADDITIONNAL_ARG}"
fi

# Set Gecko Driver path
if [[ -n ${GECKO_DRIVER} ]]; then
	GECKO_PROPERTY="-Dwebdriver.gecko.driver=${GECKO_DRIVER}"
	echo "GECKO_PROPERTY=${GECKO_PROPERTY}"
fi

# Compile framework core and samples projects
cd ${SPOT_CORE_DIR}
echo ""
echo "Compile framework core and samples projects"
${MAVEN_HOME}/bin/mvn -B -f pom.xml install -DskipTests

echo "Change directory to ${SCENARIO_DIR}"
cd ${SCENARIO_DIR}
echo "Current directory is now"
pwd

# Check whether scenario should run with Firefox 115esr
if [[ ${BROWSER} == "firefox_v115" || ${BROWSER} == "all" ]]; then
	echo ""
	echo "Run ${SCENARIO_PREFIX}Scenario using Firefox 115esr browser..."
	${MAVEN_HOME}/bin/mvn -B -f pom.xml install -Dbrowser=firefox_v115 ${GECKO_PROPERTY} ${SCENARIO_ID} ${ADDITIONNAL_ARG}
fi

# Check whether scenario should run with Firefox 102esr
if [[ ${BROWSER} == "firefox_v102" || ${BROWSER} == "all" ]]; then
	echo ""
	echo "Run ${SCENARIO_PREFIX}Scenario using Firefox 102esr browser..."
	${MAVEN_HOME}/bin/mvn -B -f pom.xml install -Dbrowser=firefox_v102 ${GECKO_PROPERTY} ${SCENARIO_ID} ${ADDITIONNAL_ARG}
fi

# Check whether scenario should run with Chrome
if [[ ${BROWSER} == "chrome" || ${BROWSER} == "all" ]]; then
	echo ""
	echo "Run ${SCENARIO_PREFIX}Scenario using Chrome browser..."
	${MAVEN_HOME}/bin/mvn -B -f pom.xml install -Dbrowser=chrome ${SCENARIO_ID} ${ADDITIONNAL_ARG}
fi

# Check whether scenario should run with Safari
if [[ ${BROWSER} == "safari" ]]; then
	echo ""
	echo "Run ${SCENARIO_PREFIX}Scenario using Safari browser..."
	${MAVEN_HOME}/bin/mvn -B -f pom.xml install -Dbrowser=safari ${SCENARIO_ID} ${ADDITIONNAL_ARG}
fi


# =============================================================
# Keep deprecated Firefox esr versions for ponctual unary tests
# =============================================================

# Check whether scenario should run with Firefox 91esr
if [[ ${BROWSER} == "firefox_v91" ]]; then
	echo ""
	echo "Run ${SCENARIO_PREFIX}Scenario using Firefox 91esr browser..."
	${MAVEN_HOME}/bin/mvn -B -f pom.xml install -Dbrowser=firefox_v91 ${GECKO_PROPERTY} ${SCENARIO_ID} ${ADDITIONNAL_ARG}
fi

# Check whether scenario should run with Firefox 78esr
if [[ ${BROWSER} == "firefox_v78" ]]; then
	echo ""
	echo "Run ${SCENARIO_PREFIX}Scenario using Firefox 78esr browser..."
	${MAVEN_HOME}/bin/mvn -B -f pom.xml install -Dbrowser=firefox_v78 ${GECKO_PROPERTY} ${SCENARIO_ID} ${ADDITIONNAL_ARG}
fi
