import io.ktor.server.engine.embeddedServer
import plugins.configureRouting
import plugins.configureSerialization
import database.DatabaseFactory
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText

fun main() {
    try {
        embeddedServer(Netty, port = 8080) {
            install(StatusPages) {
                exception<Throwable> { call, cause ->
                    println(">>> ERRO GLOBAL: ${cause::class.simpleName}: ${cause.message}")
                    cause.printStackTrace()
                    call.respondText(
                        "Erro: ${cause.message}",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
            configureSerialization()
            configureRouting()
            DatabaseFactory.init()
        }.start(wait = true)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}