#!/usr/bin/env bash

setVersion(){
  ./mvnw versions:set -DnewVersion=$1 versions:commit --no-transfer-progress
}

CURRENT_VERSION=`xmllint --xpath '/*[local-name()="project"]/*[local-name()="version"]/text()' pom.xml`

if [[ $CURRENT_VERSION == *-SNAPSHOT ]]; then
  CURRENT_BRANCH=`git branch --show-current`

	NEW_VERSION=${CURRENT_VERSION%'-SNAPSHOT'}
	NEXT_VERSION=`bash ci/semver.sh -p $NEW_VERSION`
	MAGNOLIA_VERSION=`echo $CURRENT_BRANCH | cut -f2 -d '-'`

	if [[ $MAGNOLIA_VERSION =~ ^[0-9]+_[0-9]+$ ]]; then
      NEXT_SNAPSHOT_VERSION="$NEXT_VERSION-Magnolia$MAGNOLIA_VERSION-SNAPSHOT"
      DEVELOP_BRANCH='develop-'$MAGNOLIA_VERSION
    else
      NEXT_SNAPSHOT_VERSION="$NEXT_VERSION-SNAPSHOT"
      DEVELOP_BRANCH='develop'
    fi

	echo "perform release of $NEW_VERSION from $CURRENT_VERSION and set next develop version $NEXT_SNAPSHOT_VERSION"
	echo "using current branch: $CURRENT_BRANCH and develop branch: $DEVELOP_BRANCH"

  setVersion $NEW_VERSION

 	echo "commit new release version"
	git commit -a -m "Release $NEW_VERSION: set main to new release version"

	echo "Update version in README.md"
	find . -name 'README.md' -exec sh -c "sed -i -e 's|<version>[0-9A-Za-z._-]\{1,\}</version>|<version>${NEW_VERSION}</version>|g' \"{}\" && rm -f \"{}\"-e" \;
	git commit -a -m "Release $NEW_VERSION: Update README.md"

	echo "create tag for new release"
	git tag -a $NEW_VERSION -m "Release $NEW_VERSION: tag release"

	echo "merge $CURRENT_BRANCH back to $DEVELOP_BRANCH"
	git fetch --all
	git checkout $DEVELOP_BRANCH
	git merge $CURRENT_BRANCH

  setVersion $NEXT_SNAPSHOT_VERSION

	echo "commit new snapshot version"
	git commit -a -m "Release $NEW_VERSION: set develop to next development version $NEXT_SNAPSHOT_VERSION"

	git push --all
	git push --tags
fi
