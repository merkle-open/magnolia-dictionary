#!/usr/bin/env bash

git config --global user.email "oss@namics.com"
git config --global user.name "Namics OSS CI"

ORIGIN=`git config --get remote.origin.url`
GITHUB="https://${REPO_TOKEN}@${ORIGIN#'https://'}"
git remote add github $GITHUB
git fetch github
