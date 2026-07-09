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
            val student = StudentTable
                .select { StudentTable.email eq request.email }
                .singleOrNull()

            if (student != null && BCrypt.checkpw(request.password, student[StudentTable.password])) {
                return@transaction LoginResponse(
                    userId = student[StudentTable.id].value,
                    ownerType = OwnerType.STUDENT
                )
            }

            val teacher = TeacherTable
                .select { TeacherTable.email eq request.email }
                .singleOrNull()

            if (teacher != null && BCrypt.checkpw(request.password, teacher[TeacherTable.password])) {
                return@transaction LoginResponse(
                    userId = teacher[TeacherTable.id].value,
                    ownerType = OwnerType.TEACHER
                )
            }

            null
        }
        if (result == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Email ou password incorretos")
            )
        } else {
            call.respond(HttpStatusCode.OK, result)
        }
    }
}