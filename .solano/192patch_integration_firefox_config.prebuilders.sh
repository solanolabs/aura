echo Firefox version to be used: `firefox -version`
mkdir -p $TDDIUM_REPO_ROOT/tmp
rm -rf screenshots/*
mkdir -p screenshots/$TDDIUM_SESSION_ID/$TDDIUM_TEST_EXEC_ID
touch screenshots/.holder

mvn clean install

