#!/usr/bin/env fish
set SCRIPT_DIR (cd (dirname (status -f)); and pwd)
set solutions (find $SCRIPT_DIR -name '*.sln')

for solution in $solutions;
	echo $solution
	set projectdir (dirname "$solution")
    dotnet build -c=Debug  $solution
end