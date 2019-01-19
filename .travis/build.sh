#!/usr/bin/env bash
set -e

mvn --settings .travis/settings.xml --file ./oauth2-pkce/pom.xml clean verify -DskipTests -B -V

.travis/build_client.sh
