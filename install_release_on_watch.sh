#!/bin/sh -x
adb devices | grep 4444 && adb -s localhost:4444 install -r wear/build/outputs/apk/wear-release.apk
