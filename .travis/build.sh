#!/usr/bin/env bash
set -e

mvn --settings .travis/settings.xml --file ./oauth2-pkce/pom.xml clean verify -B -V

.travis/build_client.sh
