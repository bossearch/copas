#!/usr/bin/env bash

set -e

case "$1" in
clean)
  ./gradlew clean assembleDebug
  ;;
*)
  ./gradlew assembleDebug
  ;;
esac

adb uninstall copas.app && echo "uninstalled"

adb install -r app/build/outputs/apk/debug/app-debug.apk

adb shell am start -n copas.app/.MainActivity
