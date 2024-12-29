package com.sjaindl.playintegrity.demo

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.playintegrity.v1.PlayIntegrity
import com.google.api.services.playintegrity.v1.PlayIntegrityRequestInitializer
import com.google.api.services.playintegrity.v1.model.DecodeIntegrityTokenRequest
import com.google.api.services.playintegrity.v1.model.DecodeIntegrityTokenResponse
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class PlayIntegrityDecoder {

	fun decode(token: String, requestHash: String): DecodeIntegrityTokenResponse {
		val resource = this::class.java.classLoader.getResource(SERVICE_ACCOUNT_FILENAME)
			?: throw IllegalStateException("Service account file is missing!")

		val credentials = GoogleCredentials.fromStream(resource.openStream())
		val httpRequestInitializer = HttpCredentialsAdapter(credentials)

		val playIntegrity = PlayIntegrity.Builder(NetHttpTransport(), GsonFactory(), httpRequestInitializer)
			.setApplicationName(APP_NAME)
			.setGoogleClientRequestInitializer(PlayIntegrityRequestInitializer())
			.build()

		val request = DecodeIntegrityTokenRequest().apply {
			integrityToken = token
		}

		return playIntegrity.v1().decodeIntegrityToken(PACKAGE_NAME, request).execute()
	}
}
