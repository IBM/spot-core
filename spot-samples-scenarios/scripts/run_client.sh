#!/bin/bash

# Store script dir
SCRIPT_ABSOLUTE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SCENARIO_DIR=${SCRIPT_ABSOLUTE_DIR}/..

# Read arguments
. ${SCRIPT_ABSOLUTE_DIR}/read_arguments.sh $*

# Write topology properties file
# Nothing to do so far as we're currently using a static URL...
TOPOLOGY_PROPERTIES_FILE_PATH=${SCENARIO_DIR}/params/topology.properties
echo "Topology properties file ${TOPOLOGY_PROPERTIES_FILE_PATH}:"
cat ${TOPOLOGY_PROPERTIES_FILE_PATH}

# Run scenarios
. ${SCRIPT_ABSOLUTE_DIR}/run_scenario.sh
