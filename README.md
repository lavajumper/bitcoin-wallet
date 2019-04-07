Welcome to __Sexcoin Wallet__, a standalone Sexcoin payment app for your Android device!

This work is a fork of Langerhans's Dogecoin wallet, which is a fork from Schildebach's
wallet. FOSS is a wonderful thing. 

This project contains several sub-projects:

 * __wallet__:
     The Android app itself. This is probably what you're searching for.
 * __native-scrypt__:
     Native code implementation for Scrypt. The C files are copied from the
     Java Scrypt project at [GitHub](https://github.com/wg/scrypt).
 * __native_kgw__: 
     Native code implementation for KGW calculations. C source copied and adapted for sexcoin 
     from [#E Github](https://github.com/hashengineering).
 * __market__:
     App description and promo material for the Google Play app store.
 * __integration-android__:
     A tiny library for integrating Sexcoin payments into your own Android app
     (e.g. donations, in-app purchases).
 * __sample-integration-android__:
     A minimal example app to demonstrate integration of Bitcoin payments into
     your Android app.


__Dependencies:__

This project relys on __Bitcoinj__ and __Libdohj__, both of which have been adapted for sexcoin. 
You can find these in [Lavajumper's Github](https://github.com/lavajumper)

Sexcoin's Bitcoinj was built using [bisq-networks's](https://github.com/bisq-network/bitcoinj) frozen fork.
This may change in the future.

This project also contains code from __HashEngineering__ for KGW java and native components. 
 

I think we speak for _many_ in the crypto community when we __acknowlege the fine work__ that
has gone into all these projects, and has allowed us to stand on the shoulders of some very
talented people. We would __encourage donations__ for those people who have decided to share their
work.


__Building:__

Bitcoinj and Libdohj are both maven projects. You must __build both of these and install them 
to your local maven__ repository ___before___ working on this wallet. Intellij IDEA has proven to be an
exceptional environment to accomplish this.

You can then build all sub-projects at once using Gradle:

`gradle clean build`