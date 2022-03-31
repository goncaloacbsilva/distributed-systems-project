#!/usr/bin/env bash
RED='\033[0;31m'
YELLOW='\033[0;33m'
GREEN='\033[0;32m'
RESET='\033[0m'

DIFF="diff"
if command -v colordiff 2>/dev/null >/dev/null; then
	DIFF="colordiff"
else
	echo -e "${YELLOW}Install colordiff for colored diffs${RESET}"
	echo
fi

testone() {
	local module="$1" # Professor/Client/Admin
	local test_name="$2"
	local arguments="$3"
	
	local input_file="./input/${test_name}.in"
	local expected_output_file="./expected/${test_name}.out"
	local actual_output_file="./output/${test_name}.out"
	
	local expected_line_count="$(( $(wc -l < "${expected_output_file}") + 1))"

	(
		set -e # exit (this subshell) on first error
		(cd ..; mvn -q -pl "${module}" -Dexec.args="$arguments" exec:java) < "${input_file}" > "${actual_output_file}" 2>/dev/null
		"${DIFF}" -w "${expected_output_file}" <(tail -n "${expected_line_count}" "${actual_output_file}")
	)
}

passed_count=0
test_count=0
for test_input_file in ./input/*.in; do
	# we assume test names are in the format Stuff-TheModuleThatWeNeedToRun-Stuff...
	test_name="$(basename "$test_input_file")" # extract filename from path
	test_name="${test_name%.in}" # remove extension
	module="$(echo "$test_name" | cut -d- -f2)" # extract module
	arguments="$(echo "$test_name" | cut -d- -f3- | sed "s/-/' '/" | sed "s/.*/'&'/")" # do some basic argument pre-processing: wrap each argument in quotes to deal with spaces properly

	test_count="$(( test_count + 1 ))"

	echo
	echo
	echo "Testing $test_name"
	if testone "$module" "$test_name" "$arguments"; then
		passed_count="$(( passed_count + 1 ))"
		echo -e "${GREEN}${test_name}: Success!${RESET}"
	else
		echo -e "${RED}${test_name}: Failed.${RESET}"
	fi
done

echo "Passed $passed_count of $test_count tests"
