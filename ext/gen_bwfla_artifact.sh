#!/bin/bash
set +e

FILE_PATH=$1
REPO_PATH=$2

if [ -z "$FILE_PATH" ] || [ -z "$REPO_PATH" ]
then
	echo -e "USAGE:"
	echo -e "\tgen_bwfla_artifact <FILE_PATH> <REPO_PATH>"
	echo -e "\tFILE_PATH - a relative/absolute path to a jar file"
	echo -e "\tREPO_PATH - a relative/absolute path to a repository directory"

	echo -e "\nEXAMPLES:"
	echo -e "\tgen_bwfla_artifact\tmyjar.jar\t\t\t/home/user/repo"
	echo -e "\tgen_bwfla_artifact\t/home/user/myjar-4.2.3.jar\t../repo"
	echo -e "\tgen_bwfla_artifact\t../myjar-4.2.3-SNAPSHOT.jar\t."
	echo -e "\tgen_bwfla_artifact\t/home/user/myjar-dev.jar\t/home/user/repo"
	exit 64
fi

REPO_PATH=$(readlink -f $REPO_PATH)
if [ ! -d "$REPO_PATH" ]
then
	echo "[ERROR] make sure your repository directory exists and is accessible"
	exit 65;
fi

FILE_PATH=$(readlink -f $FILE_PATH)
if [ ! -e "$FILE_PATH" ]
then
	echo "[ERROR] make sure your jar file exists and is accessible"
	exit 66;
fi
FILE_NAME=$(basename "$FILE_PATH") 


ART_NAME=$(echo $FILE_NAME | sed "s/--*[0-9][0-9]*.*\.jar$//1;s/.jar//1")
if [ -z "$ART_NAME" ]
then
	echo "[ERROR] artifact name could not be parsed properly"
	exit 67;
fi

GROUP_NAME="de.bwl.bwfla"

VERSION=$(echo $FILE_NAME | sed -n "s/\(.*\)--*\([0-9][0-9]*.*\)\.jar$/\2/1p")
if [ -z "$VERSION" ]
then
	VERSION="1.0.0"
	echo "[WARN] artifact version could not be determined, setting to: $VERSION"
fi

# DEBUG
# echo -e "$ART_NAME"
# echo -e "$GROUP_NAME"
# echo -e "$VERSION"
# echo -e "$FILE_PATH"
# echo -e "$REPO_PATH"


mvn install:install-file "-DgroupId=$GROUP_NAME" "-DartifactId=$ART_NAME" "-Dversion=$VERSION" "-Dpackaging=jar" "-Dfile=$FILE_PATH" "-DlocalRepositoryPath=$REPO_PATH"
