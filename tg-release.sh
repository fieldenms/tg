#!/bin/bash

RELEASE_VERSION=$1
NEXT_DEVELOPMENT_VERSION=$2
DATABASE_URI_PREFIX=$3
FORK_COUNT=$4

# Ensure all parameters are provided
if [ -z "$RELEASE_VERSION" ] || [ -z "$NEXT_DEVELOPMENT_VERSION" ] || [ -z "$DATABASE_URI_PREFIX" ] || [ -z "$FORK_COUNT" ]; then
  echo "Usage: $0 <release-version> <next-development-version> <database-uri-prefix> <fork-count>"
  exit 1
fi

###########################
###### Definitions ########
###########################
# Define colors
RED="\033[1;31m"
GREEN="\033[1;32m"
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
# Define styles
BOLD='\033[1m'
UNDERLINE='\033[4m'
# Reset style
NORMAL="\033[0m"

# Function to report errors
error() {
    echo -e "[${RED}${BOLD}ERROR${NORMAL}] $1"
}

# Function to report success
success() {
    echo -e "[${GREEN}${BOLD}SUCCESS${NORMAL}] $1"
}

# Function to report warnings
warn() {
    echo -e "[${YELLOW}${BOLD}WARN${NORMAL}] $1"
}

# Function to report info messages
info() {
    echo -e "[${BLUE}${BOLD}INFO${NORMAL}] $1"
}

# Function to abort release process
abort_release() {
  error "Aborting release process."
  git checkout develop
  git branch -D release-$RELEASE_VERSION
  exit 1
}

###########################
####### Releasing #########
###########################

info "Fetch latest changes"
git checkout develop && git pull origin develop || { error "Failed to fetch latest changes"; exit 1; }

info "Start the release branch"
git checkout -b release-$RELEASE_VERSION || { error "Failed to create release branch"; exit 1; }

info "Update version to release version"
mvn versions:set -DnewVersion=$RELEASE_VERSION -DprocessAllModules=true -DgenerateBackupPoms=false && \
mvn versions:commit -DgenerateBackupPoms=false || { error "Failed to update version to release version"; abort_release; }

info "Commit the changes"
git add pom.xml **/pom.xml && git commit -m "Update versions for release $RELEASE_VERSION" || { error "Failed to commit version changes"; abort_release; }

info "Merge release branch into master and tag the release"
git checkout master && git pull origin master && \
git merge --no-ff release-$RELEASE_VERSION || { error "Failed to merge release branch into master"; abort_release; }
git tag -a $RELEASE_VERSION -m "Release $RELEASE_VERSION" || { error "Failed to tag the release"; abort_release; }

info "Install locally"
if ! mvn clean install -DdatabaseUri.prefix=$DATABASE_URI_PREFIX -Dfork.count=$FORK_COUNT; then
  error "Failed to install locally. Please inspect the output for errors."
  abort_release
fi

info "Deploy the release"
if ! mvn deploy -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -DdatabaseUri.prefix=$DATABASE_URI_PREFIX -Dfork.count=$FORK_COUNT; then
  error "Failed to deploy. Please inspect the output for errors."
  abort_release
fi

info "Merge release branch back into develop"
git checkout develop && git pull origin develop && \
git merge --no-ff release-$RELEASE_VERSION || { error "Failed to merge release branch back into develop"; abort_release; }

info "Update version to next development version"
mvn versions:set -DnewVersion=$NEXT_DEVELOPMENT_VERSION -DprocessAllModules=true -DgenerateBackupPoms=false && \
mvn versions:commit -DgenerateBackupPoms=false || { error "Failed to update version to next development version"; abort_release; }

info "Commit the changes"
git add pom.xml **/pom.xml && git commit -m "Update versions to $NEXT_DEVELOPMENT_VERSION" || { error "Failed to commit next development version changes"; abort_release; }

info "Delete the release branch"
git branch -d release-$RELEASE_VERSION || { error "Failed to delete the release branch"; exit 1; }

info "Push changes to remote"
git push origin develop && git push origin master && git push origin --tags || { error "Failed to push changes to remote"; exit 1; }

success "Successfully released $RELEASE_VERSION"
