# play-integrity-demo
Play Integrity API Demo

Showcases the usage of Google Play Integrity API. 

The server accepts http or https traffic to the following endpoints:
http://localhost:8080/secure/token={play_integrity_token}/requestHash={requestHash}
https://localhost:8443/secure/token={play_integrity_token}/requestHash={requestHash}

To make your local server accessible to the Android emulator or physical device, use the following adb command to enable port forwarding:

```  adb reverse tcp:8080 tcp:8080 ```

```  adb reverse tcp:8443 tcp:8443 ``` 

The server also needs a valid Google Service Account key stored as .json-file in the resource directory.
You can follow instructions here on how to setup Google Play Console, Google Cloud Console and how to export the key: https://developer.android.com/google/play/integrity/setup

The demo is based on an integrity token requested by the Travel Companion app (see branch: https://github.com/sjaindl/TravelCompanion/tree/feat/play-integrity-api-demo).