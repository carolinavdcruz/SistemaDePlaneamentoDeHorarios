package plugins.routes.students

import database.tables.StudentTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import model.AssignTeacherRequest
import model.StudentRequest
import model.StudentResponse
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt

fun Route.studentRoutes() {
    // POST /students
    // Regista um novo aluno.
    // Body JSON esperado:
    // {
    // "name": "Ana Costa",
    // "email": "ana@alunos.isel.pt"
    // }
    post("/students") {
        val request = call.receive<StudentRequest>()
        try {
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
        } catch (e: ExposedSQLException) {
            call.respond(HttpStatusCode.Conflict, mapOf("error" to "Este email já está registado"))
        }
    }

    // GET /students
    // Lista todos os alunos.
    get("/students") {
        val students = transaction {
            StudentTable.selectAll().map { it.toStudentResponse() }
        }
        call.respond(HttpStatusCode.OK, students)
    }

    // POST /students/assign-teacher
    // Associa um professor a um aluno.
    post("/students/assign-teacher") {
        val request = call.receive<AssignTeacherRequest>()
        val updateRowStudent = transaction {
            StudentTable.update({ StudentTable.id eq request.studentId }) {
                it[teacherId] = request.teacherId
            }
        }
        if (updateRowStudent == 0) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aluno não encontrado"))
            return@post
        }
        call.respond(HttpStatusCode.OK, mapOf("message" to "Professor associado com sucesso"))
    }

    // POST /students/unassign-teacher/{studentId}
    // Desassocia o professor de um aluno.
    post("/students/unassign-teacher/{studentId}") {
        val studentId = call.parameters["studentId"]?.toIntOrNull()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "studentId inválido")
            )

        val updatedRowsStudent = transaction {
            StudentTable.update({ StudentTable.id eq studentId }) { it[teacherId] = null }
        }

        if (updatedRowsStudent == 0) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Aluno não encontrado"))
            return@post
        }

        call.respond(
            HttpStatusCode.OK,
            mapOf("message" to "Professor desassociado com sucesso")
        )
    }

    // GET /students/by-teacher/{teacherId}
    // Lista todos os alunos associados a um professor específico.
    get("/students/by-teacher/{teacherId}") {
        val teacherId = call.parameters["teacherId"]?.toIntOrNull()
            ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "teacherId inválido")
            )

        val students = transaction {
            StudentTable
                .select { StudentTable.teacherId eq teacherId }
                .map {
                    StudentResponse(
                        id = it[StudentTable.id].value,
                        name = it[StudentTable.name],
                        email = it[StudentTable.email],
                        teacherId = it[StudentTable.teacherId]?.value,
                        //password = it[StudentTable.password]
                    )
                }
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