#!/usr/bin/env bash
UNIX_TIME=$(date -u +"%s")
adb shell screencap -p /sdcard/$UNIX_TIME.png
adb pull /sdcard/$UNIX_TIME.png
adb shell rm -r /sdcard/$UNIX_TIME.png