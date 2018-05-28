#!/usr/bin/env bash

if [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
	openssl aes-256-cbc -K $encrypted_67ec11152530_key -iv $encrypted_67ec11152530_iv -in codesigning.asc.enc -out codesigning.asc -d
	gpg --fast-import ci/codesigning.asc

	mvn deploy -P ossrh --settings ci/mvnsettings.xml
fi