#!/usr/bin/env bash

# Ensure all parameters are provided
if [ "$#" -ne 5 ]; then
    echo "Usage: $0 <release-version> <next-development-version> <database-uri-prefix> <fork-count> <base-branch>"
    exit 1
fi

RELEASE_VERSION=$1
NEXT_DEVELOPMENT_VERSION=$2
DATABASE_URI_PREFIX=$3
FORK_COUNT=$4
BASE_BRANCH=$5

###########################
###### Definitions ########
###########################
if test -t 1; then
    ncolours=$(tput colors)
    if test -n "$ncolours" && test $ncolours -ge 8; then
        # Define colours
        RED="$(tput setaf 1)"
        GREEN="$(tput setaf 2)"
        YELLOW="$(tput setaf 3)"
        BLUE="$(tput setaf 4)"
        # Define styles
        BOLD="$(tput bold)"
        UNDERLINE="$(tput smul)"
        # Reset style
        NORMAL="$(tput sgr0)"
    fi
fi

# Function to report errors
error() {
    echo ""
    echo -e "[${RED}${BOLD}ERROR${NORMAL}] $1"
    echo ""
}

# Function to report success
success() {
    echo ""
    echo -e "[${GREEN}${BOLD}SUCCESS${NORMAL}] $1"
    echo ""
}

# Function to report warnings
warn() {
    echo ""
    echo -e "[${YELLOW}${BOLD}WARN${NORMAL}] $1"
    echo ""
}

# Function to report info messages
info() {
    echo ""
    echo -e "[${BLUE}${BOLD}INFO${NORMAL}] $1"
    echo ""
}

# Function to abort release process
abort_release() {
  error "Aborting release process."
  git checkout ${BASE_BRANCH}
  git branch -D release-${RELEASE_VERSION}
  # a tag may or may not be created at this stage, but just in case we need to try to delete it
  git tag -d ${RELEASE_VERSION}
  exit 1
}


##########################################
####### Pre-release verification #########
##########################################

warn "Please make sure the following parameters are suitable for the release:

       RELEASE_VERSION=${RELEASE_VERSION}
       NEXT_DEVELOPMENT_VERSION=${NEXT_DEVELOPMENT_VERSION}
       DATABASE_URI_PREFIX=${DATABASE_URI_PREFIX}
       FORK_COUNT=${FORK_COUNT}
       BASE_BRANCH=${BASE_BRANCH}
      "
read -r -p "Shall we proceed? [y/N] " answer
if [[ "$answer" =~ ^[Yy]$ ]]; then
    echo "Proceeding..."
else
    echo "Aborted."
    exit 1
fi


###########################
####### Releasing #########
###########################

info "Fetch latest changes"
git checkout ${BASE_BRANCH} && git pull origin ${BASE_BRANCH} || { error "Failed to fetch latest changes"; exit 1; }

info "Start the release branch"
git checkout -b release-${RELEASE_VERSION} || { error "Failed to create release branch"; exit 1; }

info "Update module versions to release version ${RELEASE_VERSION}"
mvn versions:set -DnewVersion=${RELEASE_VERSION} -DprocessAllModules=true -DgenerateBackupPoms=false && \
    mvn versions:commit -DgenerateBackupPoms=false || { error "Failed to update version to release version"; abort_release; }

info "Commit the changes"
git add pom.xml **/pom.xml && git commit -m "Update versions for release ${RELEASE_VERSION}" || { error "Failed to commit version changes"; abort_release; }

# Skip merging into master. Such si
read -r -p "Shall skip or merge into master? [s/M] " mergeAnswer
if [[ "$mergeAnswer" =~ ^[Mm]$ ]]; then
    info "Merge release branch into master."
    git checkout master && git pull origin master && \
    git merge --no-ff release-${RELEASE_VERSION} -m "Merged ${RELEASE_VERSION} into master." || { error "Failed to merge release branch into master"; abort_release; }
else
    info "Skipped merging release branch into master."
fi

info "Tag the release ${RELEASE_VERSION}"
git tag -a ${RELEASE_VERSION} -m "Release ${RELEASE_VERSION}" || { error "Failed to tag the release"; abort_release; }

read -r -s -p $'Press ENTER to build and deploy...'

info "Deploy the release"
if ! mvn clean deploy -DdatabaseUri.prefix=${DATABASE_URI_PREFIX} -Dfork.count=${FORK_COUNT}; then
  error "Failed to deploy. Please inspect the output for errors."
  abort_release
fi

success "Deployed ${RELEASE_VERSION}."

read -r -s -p "Press ENTER to merge the release branch into ${BASE_BRANCH} (local)..."

info "Merge release branch back into ${BASE_BRANCH}"
git checkout ${BASE_BRANCH} && git pull origin ${BASE_BRANCH} && \
    git merge --no-ff release-${RELEASE_VERSION} || { error "Failed to merge release branch back into ${BASE_BRANCH}"; abort_release; }

info "Update version to next development version ${NEXT_DEVELOPMENT_VERSION}"
mvn versions:set -DnewVersion=${NEXT_DEVELOPMENT_VERSION} -DprocessAllModules=true -DgenerateBackupPoms=false && \
    mvn versions:commit -DgenerateBackupPoms=false || { error "Failed to update version to next development version"; abort_release; }

info "Commit the changes"
git add pom.xml **/pom.xml && git commit -m "Update versions to ${NEXT_DEVELOPMENT_VERSION}" || { error "Failed to commit next development version changes"; abort_release; }

read -r -s -p "Press ENTER to delete the release branch..."

info "Delete the release branch ${RELEASE_VERSION}"
git branch -d release-${RELEASE_VERSION} || { error "Failed to delete the release branch"; exit 1; }

read -r -s -p "Press ENTER to push changes to remote - make sure your have the privileges for that..."


if [[ "$mergeAnswer" =~ ^[Mm]$ ]]; then
    info "Push changes to remote - ${BASE_BRANCH}, master, and tags."
    git push origin ${BASE_BRANCH} && git push origin master && git push origin --tags || { error "Failed to push changes to remote"; exit 1; }
else
    info "Push changes to remote - ${BASE_BRANCH} and tags (but not master)."
    git push origin ${BASE_BRANCH} && git push origin --tags || { error "Failed to push changes to remote"; exit 1; }
fi

success "Successfully released ${RELEASE_VERSION}"
