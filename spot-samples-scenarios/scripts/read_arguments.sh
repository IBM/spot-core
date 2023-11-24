#!/bin/bash

# TODO
function usage() {
	echo "Usage run_client.bat -b browser_type -s scenario_suffix"
	echo "* browser_type: can be either 'all', 'firefox_v115', 'firefox_v102', 'firefox_v91', 'firefox_v78' or 'chrome'"
}

# Store script dir
SPOT_SCRIPTS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SPOT_CORE_DIR="$( cd ${SPOT_SCRIPTS_DIR}/../.. && pwd )"
echo "SPOT core root dir is ${SPOT_CORE_DIR}"

# Read arguments
while getopts ":b:s:m:n:p:a:h:o:e:t:" opt; do
	case $opt in 
		b)
			BROWSER_ARG=${OPTARG}
			;;
		s)
			SCENARIO_ARG=${OPTARG}
			;;
		p)
			ADDITIONNAL_PARAMETERS_ARG=${OPTARG}
			;;
		a)
			ADDITIONNAL_ARGUMENTS_ARG="${ADDITIONNAL_ARGUMENTS_ARG} ${OPTARG}"
			;;
		h)
			HOSTNAME_ARG=${OPTARG}
			;;
		o)
			PORT_ARG=${OPTARG}
			;;
		t)
			TOPOLOGY_ARG=${OPTARG}
			;;
		\?)
			msg="Invalid option: -${OPTARG}"
			echo ${msg}; echo ${msg} > ${SPOT_SCRIPTS_DIR}/setup.err
			exit 1
			;;
		:)
			msg="Option -${OPTARG} requires an argument."
			echo ${msg}; echo ${msg} > ${SPOT_SCRIPTS_DIR}/setup.err
			exit 1
			;;
	esac
done

# Read browser argument
if [[ -z ${BROWSER_ARG} ]]; then
	msg="Missing browser argument!"
	echo ${msg}; echo ${msg} > ${SPOT_SCRIPTS_DIR}/setup.err
	exit 1
fi
case ${BROWSER_ARG} in
	"all" )
		NEED_GECKO_DRIVER="true"
		;;
	"firefox_v78" | "firefox_v91" | "firefox_v102" | "firefox_v115" )
		NEED_GECKO_DRIVER="true"
		;;
	"chrome" | "safari" )
		;;
	*)
		msg="'${BROWSER-ARG}' is not a valid browser type. Expecting either all, firefox_v115, firefox_v102, firefox_v91, firefox_v78, chrome or safari"
		echo ${msg}; echo ${msg} > ${SPOT_SCRIPTS_DIR}/setup.err
		exit 1
esac
BROWSER=${BROWSER_ARG}
echo "SPOT scenarios will be run with ${BROWSER} browser(s)"

# Check Gecko driver system property if specified
if [[ ${NEED_GECKO_DRIVER} == "true" ]]; then
	echo ""
	if [[ -n ${GECKO_DRIVER} ]]; then
		echo " - Gecko driver location will be ${GECKO_DRIVER}"
		if [[ ! -f ${GECKO_DRIVER} ]]; then
			echo "Cannot find gecko driver file on local drive."
			exit 1
		fi
	else
		echo "- Gecko driver path not defined by an environment variable, assuming it will be set using properties file."
	fi
fi

# Scenario prefix and identifier
if [[ -z ${SCENARIO_ARG} ]]; then
	echo "Missing scenario argument!"
	exit 1
fi
SCENARIO_PREFIX="${SCENARIO_ARG}"
echo "SCENARIO_PREFIX=${SCENARIO_PREFIX}"
SCENARIO_ID="-Dscenario_identifier=${SCENARIO_PREFIX}"
echo "SCENARIO_ID=${SCENARIO_ID}"
