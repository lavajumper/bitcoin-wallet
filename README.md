###Sexcoin Wallet
 A standalone Sexcoin payment app for your Android device!

_This project would not have been made had it not been for Andreas Schildbach, the Litecoin Dev Team, HashEngineering, and Nite69.
**Thank you for sharing your hard work!**_


This project contains several sub-projects:

 * __wallet__:
     The Android app itself. This is probably what you're searching for.
 * __integration-android__:
     A tiny library for integrating Sexcoin payments into your own Android app
     (e.g. donations, in-app purchases).
 * __sample-integration-android__:
     A minimal example app to demonstrate integration of Sexcoin payments into
     your Android app.


The wallet project relies on __sexcoinj-0.12.5-SNAPSHOT__. This is NOT in a maven repository. The easiest way to integrate is to pull the source for sexcoinj (https://github.com/lavajumper/sexcoinj-scrypt), and run:

`mvn clean install -Dmaven.test.skip=true`

This will place sexcoinj-0.12.5-SNAPSHOT.jar and sources in your local ( .m2 ) repository. Please note that sexcoinj is only valid for projects that don't require diffculty checks.

You can then build all sub-projects at once using Maven:

`mvn clean install`


The source for sexcoinj is https://github.com/lavajumper/sexcoinj-scrypt, __use the sexcoin branch__

*Note for devs who don't LIVE in AndroidLand*: __Intellij IDE__ was the way to go for this project.
