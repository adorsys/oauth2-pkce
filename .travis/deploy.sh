#!/usr/bin/env bash
set -e

mvn --settings .travis/settings.xml --file ./oauth2-pkce/pom.xml package gpg:sign deploy -Prelease -DskipTests -B -U;
