package plugin

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import model.AssignTeacherRequest
import model.StudentRequest
import model.StudentResponse
import model.TeacherRequest
import model.TeacherResponse
import plugins.configureRouting
import plugins.configureSerialization
import testsuport.TestDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StudentRoutesTest {

    @BeforeTest
    fun setup() {
        TestDatabase.reset()
    }

    private fun ApplicationTestBuilder.jsonClient(): HttpClient {
        application {
            configureSerialization()
            configureRouting()
        }
        return createClient {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    @Test
    fun `get students devolve lista vazia inicialmente`() = testApplication {
        val client = jsonClient()

        val response = client.get("/students")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(emptyList<StudentResponse>(), response.body())
    }

    @Test
    fun `get students devolve alunos criados`() = testApplication {
        val client = jsonClient()

        client.post("/students") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRequest(
                    name = "Aluno A",
                    email = "a@isel.pt",
                    password = "Student123"
                )
            )
        }

        client.post("/students") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRequest(
                    name = "Aluno B",
                    email = "b@isel.pt",
                    password = "Student123"
                )
            )
        }

        val response = client.get("/students")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<List<StudentResponse>>()
        assertEquals(2, body.size)
    }

    @Test
    fun `unassign teacher remove professor do aluno`() = testApplication {
        val client = jsonClient()

        val teacher = client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. Joao",
                    email = "joao@isel.pt",
                    password = "Teacher123"
                )
            )
        }.body<TeacherResponse>()

        val student = client.post("/students") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRequest(
                    name = "Aluno A",
                    email = "a@isel.pt",
                    password = "Student123",
                    teacherId = teacher.id
                )
            )
        }.body<StudentResponse>()

        val unassignResponse = client.post("/students/unassign-teacher/${student.id}")

        assertEquals(HttpStatusCode.OK, unassignResponse.status)

        val byTeacherResponse = client.get("/students/by-teacher/${teacher.id}")
        val students = byTeacherResponse.body<List<StudentResponse>>()

        assertTrue(students.isEmpty())
    }

    @Test
    fun `unassign teacher com aluno inexistente devolve 404`() = testApplication {
        val client = jsonClient()

        val response = client.post("/students/unassign-teacher/999")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("Aluno não encontrado"))
    }

    @Test
    fun `unassign teacher com studentId invalido devolve 400`() = testApplication {
        val client = jsonClient()

        val response = client.post("/students/unassign-teacher/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("studentId inválido"))
    }

    @Test
    fun `by teacher com teacherId invalido devolve 400`() = testApplication {
        val client = jsonClient()

        val response = client.get("/students/by-teacher/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("teacherId inválido"))
    }

    @Test
    fun `by teacher devolve lista vazia quando professor nao tem alunos`() = testApplication {
        val client = jsonClient()

        val teacher = client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. Pedro",
                    email = "pedro@isel.pt",
                    password = "Teacher123"
                )
            )
        }.body<TeacherResponse>()

        val response = client.get("/students/by-teacher/${teacher.id}")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(emptyList<StudentResponse>(), response.body())
    }

    @Test
    fun `assign teacher com aluno inexistente devolve 404`() = testApplication {
        val client = jsonClient()

        val teacher = client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. Joao",
                    email = "joao2@isel.pt",
                    password = "Teacher123"
                )
            )
        }.body<TeacherResponse>()

        val response = client.post("/students/assign-teacher") {
            contentType(ContentType.Application.Json)
            setBody(
                AssignTeacherRequest(
                    studentId = 999,
                    teacherId = teacher.id
                )
            )
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("Aluno não encontrado"))
    }
}