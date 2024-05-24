#!/usr/bin/env bash


function publish() {
  PROJECT=$1
  ./gradlew $PROJECT:clean $PROJECT:build $PROJECT:sign $PROJECT:publish
}

publish fyeolib
publish aws-secretmanager
publish flyway-plugin