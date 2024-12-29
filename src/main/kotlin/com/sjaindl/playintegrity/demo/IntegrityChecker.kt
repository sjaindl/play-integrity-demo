package com.sjaindl.playintegrity.demo

import com.google.api.services.playintegrity.v1.model.TokenPayloadExternal

enum class IntegrityStatus {
    PASS,
    FAIL,
    ACTION_NEEDED
}

// Details see: https://developer.android.com/google/play/integrity/verdicts
class IntegrityChecker {
    fun checkIntegrity(payload: TokenPayloadExternal, expectedRequestHash: String): IntegrityStatus {

        // 1. Check request details for developer-provided hash, matching package name, and timestamp
        with(payload.requestDetails) {
            if (requestHash != expectedRequestHash || requestPackageName != PACKAGE_NAME) {
                return IntegrityStatus.FAIL
            }

            if (System.currentTimeMillis() - timestampMillis > ALLOWED_WINDOW_MILLIS) {
                return IntegrityStatus.FAIL
            }
        }

        // 2. Check device recognition verdict
        val deviceRecognitionVerdict = payload.deviceIntegrity?.deviceRecognitionVerdict
        if (deviceRecognitionVerdict == null || !deviceRecognitionVerdict.contains("MEETS_DEVICE_INTEGRITY")) {
            println("Device recognition verdict does not meet integrity. Verdict: $deviceRecognitionVerdict")
            return IntegrityStatus.FAIL
        }

        // 3. Check app recognition verdict
        // Possible values: PLAY_RECOGNIZED, UNRECOGNIZED_VERSION, or UNEVALUATED
        if (payload.appIntegrity?.appRecognitionVerdict != "PLAY_RECOGNIZED") {
            return IntegrityStatus.FAIL
        }

        // 4. Check for app licensing verdict
        // Could be LICENSED, UNLICENSED, or UNEVALUATED
        with(payload.accountDetails) {
            return when (appLicensingVerdict) {
                "UNLICENSED" -> IntegrityStatus.ACTION_NEEDED // app not acquired from Google Play
                "UNEVALUATED" -> IntegrityStatus.FAIL
                else -> IntegrityStatus.PASS
            }
        }
    }
}
