#!/usr/bin/env bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )")

solutions=$(find $SCRIPT_DIR -name '*.sln')

for solution in $solutions; do
	echo $solution
	projectdir="$(dirname "$solution")"
	dotnet build -c=Debug $solution
done