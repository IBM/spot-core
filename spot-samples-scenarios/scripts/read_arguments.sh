#!/bin/bash

# TODO
function usage() {
	echo "Usage run_client.bat -b browser_type -s scenario_suffix"
	echo "* browser_type: can be either 'all', 'firefox_v60', 'firefox_v52' or 'chrome'"
}

# Store script dir
SPOT_SCRIPTS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SPOT_CORE_DIR="$( cd ${SPOT_SCRIPTS_DIR}/../.. && pwd )"
echo "SPOT core root dir is ${SPOT_CORE_DIR}"

# Read arguments
while getopts ":b:s:g:m:n:p:a:h:o:e:t:" opt; do
	case $opt in 
		b)
			BROWSER_ARG=${OPTARG}
			;;
		s)
			SCENARIO_ARG=${OPTARG}
			;;
		g)
			GECKO_ARG=${OPTARG}
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
#		FIRST_TEST_ARG=-DfirstTest=test05
		;;
	"firefox_v60" | "firefox_v68" )
		NEED_GECKO_DRIVER="true"
		;;
	"chrome" | "safari" )
		;;
	*)
		msg="'${BROWSER-ARG}' is not a valid browser type. Expecting either all, firefox_v68, firefox_v60, chrome or safari"
		echo ${msg}; echo ${msg} > ${SPOT_SCRIPTS_DIR}/setup.err
		exit 1
esac
BROWSER=${BROWSER_ARG}
echo "SPOT scenarios will be run with ${BROWSER} browser(s)"

# Add Gecko driver system property if required
if [[ ${NEED_GECKO_DRIVER} == "true" ]]; then
	echo ""
	echo "Add Gecko driver system property when starting Maven command..."
	if [[ -n ${GECKO_ARG} ]]; then
		GECKO_DRIVER="${GECKO_ARG}"
	fi
	if [[ -z ${GECKO_DRIVER} ]]; then
		echo "Missing Gecko driver path."
		exit 1
	fi
	echo " - Gecko driver location will be ${GECKO_DRIVER}"
	if [[ ! -f ${GECKO_DRIVER} ]]; then
		echo "Cannot find gecko driver file on local drive."
		exit 1
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
