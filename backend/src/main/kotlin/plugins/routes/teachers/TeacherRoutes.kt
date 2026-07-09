package plugins.routes.teachers

import database.tables.TeacherTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import model.TeacherRequest
import model.TeacherResponse
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun Route.teacherRoutes() {

    // POST /teachers
    // Regista um novo professor.
    // Body JSON esperado:
    // {
    //   "name": "João Silva",
    //   "email": "joao@isel.pt",
    // }
    post("/teachers") {
        val request = call.receive<TeacherRequest>()
        val created = transaction {
            val id = TeacherTable.insert {
                it[name] = request.name
                it[email] = request.email
                it[password] = BCrypt.hashpw(request.password, BCrypt.gensalt())
            } get TeacherTable.id
            TeacherResponse(id = id.value, name = request.name, email = request.email)
        }
        call.respond(HttpStatusCode.Created, created)
    }

    // GET /teachers
    // Lista todos os professores.
    get("/teachers") {
        val teachers = transaction {
            TeacherTable.selectAll().map {
                TeacherResponse(
                    id = it[TeacherTable.id].value,
                    name = it[TeacherTable.name],
                    email = it[TeacherTable.email]
                )
            }
        }
        call.respond(HttpStatusCode.OK, teachers)
    }
}