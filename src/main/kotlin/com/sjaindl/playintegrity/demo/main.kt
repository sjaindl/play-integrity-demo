package com.sjaindl.playintegrity.demo

import io.ktor.http.HttpStatusCode
import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.*
import java.io.*

fun main() {
    embeddedServer(
        factory = Netty,
        environment = applicationEnvironment {
            log = LoggerFactory.getLogger("ktor.application")
        },
        configure = {
            envConfig()
        },
        module = Application::module,
    ).start(wait = true)
}

private fun ApplicationEngine.Configuration.envConfig() {

    val keyStoreFile = File("build/keystore.jks")
    val keyStore = buildKeyStore {
        certificate("sampleAlias") {
            password = "foobar"
            domains = listOf("127.0.0.1", "0.0.0.0", "localhost")
        }
    }
    keyStore.saveToFile(keyStoreFile, "123456")

    connector {
        port = 8080
    }
    sslConnector(
        keyStore = keyStore,
        keyAlias = "sampleAlias",
        keyStorePassword = { "123456".toCharArray() },
        privateKeyPassword = { "foobar".toCharArray() }
    ) {
        port = 8443
        keyStorePath = keyStoreFile
    }
}

fun Application.module() {
    routing {
        get("/") {
            call.respondText("Hello, world!")
        }

        get("/secure") {
            val token = call.request.queryParameters["token"]
            val requestHash = call.request.queryParameters["requestHash"]

            if (token == null || requestHash == null) {
                call.respond(HttpStatusCode.BadRequest, "token or request hash not provided")
            } else {
                val integrityStatus: IntegrityStatus = try {
                    val response = PlayIntegrityDecoder().decode(token = token, requestHash = requestHash)
                    IntegrityChecker().checkIntegrity(payload = response.tokenPayloadExternal, expectedRequestHash = requestHash)
                } catch (exception: IOException) {
                    IntegrityStatus.FAIL
                }

                when (integrityStatus) {
                    IntegrityStatus.PASS -> {
                        call.respond(performRequestWithApiKey())
                    }
                    IntegrityStatus.FAIL -> call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = "Play Integrity checks failed"
                    )
                    IntegrityStatus.ACTION_NEEDED -> call.respond(
                        status = HttpStatusCode.Forbidden,
                        message = "Play Integrity checks failed"
                    )
                }
            }
        }
    }
}

fun performRequestWithApiKey(): String {
    val key = SECRET_API_KEY
    // ... use key to perform request

    return "Demo response using API key from server"
}
