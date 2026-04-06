import database.DatabaseFactory
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = 8080) {
        //configureSerialization()
        //configureRouting()
        DatabaseFactory.init()
    }.start(wait = true)
}