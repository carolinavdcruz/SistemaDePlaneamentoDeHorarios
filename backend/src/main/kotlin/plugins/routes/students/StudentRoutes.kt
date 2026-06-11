package plugins.routes.students

import database.tables.StudentTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import model.AssignTeacherRequest
import model.StudentRequest
import model.StudentResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import plugins.intParam

fun Route.studentRoutes() {
    post("/students") {
        val request = call.receive<StudentRequest>()
        val created = transaction {
            val id = StudentTable.insert {
                it[name] = request.name
                it[email] = request.email
                it[password] = BCrypt.hashpw(request.password, BCrypt.gensalt())
                it[teacherId] = request.teacherId
            } get StudentTable.id
            StudentResponse(id = id.value, name = request.name, email = request.email, teacherId = request.teacherId)
        }
        call.respond(HttpStatusCode.Created, created)
    }

    get("/students") {
        val students = transaction {
            StudentTable.selectAll().map { it.toStudentResponse() }
        }
        call.respond(HttpStatusCode.OK, students)
    }

    post("/students/assign-teacher") {
        val request = call.receive<AssignTeacherRequest>()
        val updated = transaction {
            StudentTable.update({ StudentTable.id eq request.studentId }) {
                it[teacherId] = request.teacherId
            }
        }
        if (updated == 0) return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aluno não encontrado"))
        call.respond(HttpStatusCode.OK, mapOf("message" to "Professor associado com sucesso"))
    }

    post("/students/unassign-teacher/{studentId}") {
        val studentId = call.intParam("studentId") ?: return@post
        val updated = transaction {
            StudentTable.update({ StudentTable.id eq studentId }) { it[teacherId] = null }
        }
        if (updated == 0) return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aluno não encontrado"))
        call.respond(HttpStatusCode.OK, mapOf("message" to "Professor desassociado com sucesso"))
    }

    get("/students/by-teacher/{teacherId}") {
        val teacherId = call.intParam("teacherId") ?: return@get
        val students = transaction {
            StudentTable.select { StudentTable.teacherId eq teacherId }.map { it.toStudentResponse() }
        }
        call.respond(HttpStatusCode.OK, students)
    }
}

// Extensão local para evitar repetição do mapeamento
private fun org.jetbrains.exposed.sql.ResultRow.toStudentResponse() = StudentResponse(
    id = this[StudentTable.id].value,
    name = this[StudentTable.name],
    email = this[StudentTable.email],
    teacherId = this[StudentTable.teacherId]?.value
)