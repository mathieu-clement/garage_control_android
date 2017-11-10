#!/bin/sh -e

adb forward tcp:4444 localabstract:/adb-hub
adb connect localhost:4444
sleep 3
adb devices
