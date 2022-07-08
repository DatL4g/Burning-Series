# BurningSeries-Android

[![Issues](https://img.shields.io/github/issues/DATL4G/BurningSeries-Android.svg?style=for-the-badge)](https://github.com/DATL4G/BurningSeries-Android/issues)
[![Stars](https://img.shields.io/github/stars/DATL4G/BurningSeries-Android.svg?style=for-the-badge)](https://github.com/DATL4G/BurningSeries-Android/stargazers)
[![Forks](https://img.shields.io/github/forks/DATL4G/BurningSeries-Android.svg?style=for-the-badge)](https://github.com/DATL4G/BurningSeries-Android/network/members)
[![Contributors](https://img.shields.io/github/contributors/DATL4G/BurningSeries-Android.svg?style=for-the-badge)](https://github.com/DATL4G/BurningSeries-Android/graphs/contributors)
[![License](https://img.shields.io/github/license/DATL4G/BurningSeries-Android.svg?style=for-the-badge)](https://github.com/DATL4G/BurningSeries-Android/blob/master/LICENSE)
[![Github all releases](https://img.shields.io/github/downloads/DATL4G/BurningSeries-Android/total.svg?style=for-the-badge)](https://github.com/DATL4G/BurningSeries-Android/releases)

[![CodeFactor](https://www.codefactor.io/repository/github/datl4g/burningseries-android/badge)](https://sonarcloud.io/project/overview?id=DATL4G_BurningSeries-Android)
[![Release Status](https://github.com/DatL4g/BurningSeries-Android/actions/workflows/release.yml/badge.svg)](https://github.com/DATL4G/BurningSeries-Android/actions/workflows/release.yml)

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&logo=android-studio&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
![SQLite](https://img.shields.io/badge/sqlite-%2307405e.svg?style=for-the-badge&logo=sqlite&logoColor=white)

Watch any series or videos on [Burning Series](https://bs.to/) easily with this app.
You can see the latest series and episodes, get an overview of all available series, save your favorites and start watching directly inside the app.
**You can't login to your [Burning Series](https://bs.to/) Account because it's not using an official API.** Read the [Important Notice](#important-notice) for more info.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="60">](https://f-droid.org/packages/de.datlag.burningseries/)

- [Important Notice](#important-notice)
- [Motivation](#motivation)
- [Used Technologies](#used-technologies)
- [Supported devices and Installation](#supported-devices-and-installation)
- [Usage](#usage)
- [Contributing](#contributing)
  - [Maintainers](#maintainers)
- [Support the project](#support-the-project)

## Important Notice

This app is **NOT OFFICIAL** by [Burning Series](https://bs.to/)!!!

This is a personal project and doesn't rely on any official API by [Burning Series](https://bs.to/), that means it could break any time.
It's not guaranteed that this project will be maintained, especially not by any owner or developer of [Burning Series](https://bs.to/).
If the app stops working don't report that in any form on [Burning Series](https://bs.to/), instead open an [Issue](https://github.com/DATL4G/BurningSeries-Android/issues/new/choose) or fix it yourself and create a pull request here.

**This project is not related to any owner, developer or other staff from [Burning Series](https://bs.to/)**

## Motivation

Why did I create this app?
Why not. Well as a developer you always search for new stuff to create and try to overcome problems you face while coding.

It all started by how annoyed I was when I tried to navigate on the [Burning Series](https://bs.to/) website while using a phone.
So I thought we need an app for this. I searched for an official API and any stuff related to this but I couldn't find something.

*That's where it ended.* Well... until my developer mentality came out and I had to find a workaround.

When you can't find an official API build it yourself. At least that's what I did here.
I started getting the data I needed from the website step by step until I could use and show all the data in an app.

## Used Technologies
**If you aren't interested in development or how the app works under the hood you can skip this part :)**

The app uses a self build API using site scraping. This is done on multiple ways.
First it tries to do that on the device, however if the site can't be reached because it's blocked by the users DNS for example then it fetches the data using a fallback built with [WrapAPI](https://wrapapi.com/).
This way the user doesn't need a custom DNS or a VPN or any other type of workaround if [Burning Series](https://bs.to/) is blocked by their internet provider. Learn more about usual problems here [Burning Series Domains](https://burningseries.domains/).
(That's the good part of an unofficial API!)
Scraping the data using [WrapAPI](https://wrapapi.com/) however is only done once a day to prevent exceeding the API rate limit.

The data is then stored in a local database on the device using [Room](https://developer.android.com/jetpack/androidx/releases/room) so the app can be used in offline mode and doesn't need to get new data every time.
Images are stored in separate files on the device for the same reason.

The data is emitted to the view lifecycle using [Flow](https://kotlinlang.org/docs/flow.html), you might find some small places where [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) is used however I won't use [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) anymore and migrate existing usage to [Flow](https://kotlinlang.org/docs/flow.html) as it's easier to use and maintain.

Settings are saved using [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) with [Protocol Buffers](https://developers.google.com/protocol-buffers).

**Pull Requests relying on [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) or [Shared Preferences](https://developer.android.com/reference/android/content/SharedPreferences) or [Data Store Preferences](https://developer.android.com/topic/libraries/architecture/datastore#datastore-preferences) won't be accepted!**

## Supported devices and Installation

All Android smartphones with version >=5.0 (Lollipop and up) as well as Android TVs are supported.

To install the app head over to the [Release Section](https://github.com/DATL4G/BurningSeries-Android/releases) and download the ```.apk``` file. For more advanced users is an ```.aab``` file provided which can be installed using [bundletool](https://developer.android.com/studio/command-line/bundletool).
Installing the ```.apk``` file is easy, just copy it to your phone (or download it directly on your phone) and open it to install. If this won't work you can use [ADB](https://developer.android.com/studio/command-line/adb) sideloading to install it from your pc on your phone.

You can also download the app from [F-Droid](https://f-droid.org/packages/de.datlag.burningseries/).

Search on the internet if you face any problems. Don't ask for help here!

## Usage

The usage is pretty self explanatory the screenshots below show some examples.

| Home | All | Favorites | Series |
|---|---|---|---|
| ![Home Dark](https://github.com/DATL4G/BurningSeries-Android/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png) | ![All Dark](https://github.com/DATL4G/BurningSeries-Android/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/6.png) | ![Favorites Dark](https://github.com/DATL4G/BurningSeries-Android/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/7.png) | ![Series Dark](https://github.com/DATL4G/BurningSeries-Android/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/8.png) |
| ![Home Light](https://github.com/DATL4G/BurningSeries-Android/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png) | ![All Light](https://github.com/DATL4G/BurningSeries-Android/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png) | ![Favorites Light](https://github.com/DATL4G/BurningSeries-Android/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png) | ![Series Light](https://github.com/DATL4G/BurningSeries-Android/blob/master/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png) |

## Contributing

When you face any bugs or problems please open an [Issue](https://github.com/DATL4G/BurningSeries-Android/issues/new/choose).

To add functionality fork the project and create a pull request afterwards. You should know how that works if you are a developer :)
You can add yourself to the list below if you want then.

### Maintainers

| Avatar | Contributor |
|---|:---:|
| [![](https://avatars3.githubusercontent.com/u/46448715?s=50&v=4)](http://github.com/DatL4g) | [DatLag](http://github.com/DatL4g) |

## Support the project

[![Github-sponsors](https://img.shields.io/badge/sponsor-30363D?style=for-the-badge&logo=GitHub-Sponsors&logoColor=#EA4AAA)](https://github.com/sponsors/DATL4G)
[![PayPal](https://img.shields.io/badge/PayPal-00457C?style=for-the-badge&logo=paypal&logoColor=white)](https://paypal.me/datlag)
[![Patreon](https://img.shields.io/badge/Patreon-F96854?style=for-the-badge&logo=patreon&logoColor=white)](https://www.patreon.com/datlag)

Supporting this project helps to keep it up-to-date. You can donate if you want or contribute to the project as well.
This shows that the app is used by people and it's worth to maintain.

Another way of supporting is to actually use the app and activate hoster, so you and other people can start watching without bothering.
