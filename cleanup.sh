#!/bin/sh
rm -rf .idea
./gradlew clean
rm -rf .gradle
rm -rf build
rm -rf */build
rm -rf app/iosApp/iosApp.xcworkspace
rm -rf app/iosApp/Pods
rm -rf app/iosApp/iosApp.xcodeproj/project.xcworkspace
rm -rf app/iosApp/iosApp.xcodeproj/xcuserdata
