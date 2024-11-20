#!/usr/bin/env bash

# Ensure all parameters are provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <new-version>"
    exit 1
fi

NEW_VERSION=$1

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

###########################
######## Updating #########
###########################

info "Update module versions to ${NEW_VERSION}"
mvn versions:set -DnewVersion=${NEW_VERSION} -DprocessAllModules=true -DgenerateBackupPoms=false && \
    mvn versions:commit -DgenerateBackupPoms=false || { error "Failed to update the version to ${NEW_VERSION}."; }

success "Successfully updated version to ${NEW_VERSION}."

