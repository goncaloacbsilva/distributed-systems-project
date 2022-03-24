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
	
	local input_file="./input/${test_name}.in"
	local expected_output_file="./expected/${test_name}.out"
	local actual_output_file="./output/${test_name}.out"
	
	local expected_line_count="$(( $(wc -l < "${expected_output_file}") + 1))"

	local student_id=""
	local student_name=""

  if [ "$test_name" = "02-student" ]; then
    # Cristina Ferreira
    student_id="aluno1000"
    student_name="'Cristina Ferreira'"
  elif [ "$test_name" = "03-student" ]; then
    # Goucha
    student_id="aluno1001"
    student_name="'Manuel Goucha'"
  elif [ "$test_name" = "05-student" ]; then
    student_id="aluno1001"
    student_name="'Manuel Goucha'"
  elif [ "$test_name" = "07-student" ]; then
    # 3a pessoa
    student_id="aluno1002"
    student_name="'Castelo Branco'"
  fi

  if [ "$student_id" != "" ]; then
    local args="-Dexec.args=localhost 2001 ${student_id} ${student_name}"
    echo "Using ARGS: $args"
    (
    		set -e # exit (this subshell) on first error
    		(cd ..; mvn -q -pl "${module}" exec:java "${args}") < "${input_file}" > "${actual_output_file}" 2>/dev/null
    		"${DIFF}" -w "${expected_output_file}" <(tail -n "${expected_line_count}" "${actual_output_file}")
    	)
  else
    (
        set -e # exit (this subshell) on first error
        (cd ..; mvn -q -pl "${module}" exec:java) < "${input_file}" > "${actual_output_file}" 2>/dev/null
        "${DIFF}" -w "${expected_output_file}" <(tail -n "${expected_line_count}" "${actual_output_file}")
      )
  fi
}

passed_count=0
test_count=0
for test_input_file in ./input/*.in; do
	# we assume test names are in the format Stuff-TheModuleThatWeNeedToRun-Stuff...
	test_name="$(basename "$test_input_file")" # extract filename from path
	test_name="${test_name%.in}" # remove extension
	module="$(echo "$test_name" | cut -d- -f2)" # extract module
	# module="${module^}" # make first letter of module uppercase
	module="$(tr '[:lower:]' '[:upper:]' <<< "${module:0:1}")${module:1}"

	test_count="$(( test_count + 1 ))"

	if testone "$module" "$test_name"; then
		passed_count="$(( passed_count + 1 ))"
		echo -e "${GREEN}${test_name}: Success!${RESET}"
	else
		echo -e "${RED}${test_name}: Failed.${RESET}"
	fi
done

echo "Passed $passed_count of $test_count tests"
