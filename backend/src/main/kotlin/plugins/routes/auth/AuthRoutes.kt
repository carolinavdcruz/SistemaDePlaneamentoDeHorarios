package plugins.routes.auth

import database.tables.StudentTable
import database.tables.TeacherTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import model.LoginRequest
import model.LoginResponse
import model.OwnerType
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun Route.authRoutes() {
    post("/login") {
        val request = call.receive<LoginRequest>()

        val result = transaction {
            StudentTable.select { StudentTable.email eq request.email }
                .singleOrNull()
                ?.takeIf { BCrypt.checkpw(request.password, it[StudentTable.password]) }
                ?.let { return@transaction LoginResponse(it[StudentTable.id].value, OwnerType.STUDENT) }

            TeacherTable.select { TeacherTable.email eq request.email }
                .singleOrNull()
                ?.takeIf { BCrypt.checkpw(request.password, it[TeacherTable.password]) }
                ?.let { return@transaction LoginResponse(it[TeacherTable.id].value, OwnerType.TEACHER) }

            null
        }

        if (result == null)
            return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Email ou password incorretos"))

        call.respond(HttpStatusCode.OK, result)
    }
}