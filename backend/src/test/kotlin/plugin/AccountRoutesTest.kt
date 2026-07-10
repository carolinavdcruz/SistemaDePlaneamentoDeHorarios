package plugin

import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import model.AssignTeacherRequest
import model.LoginRequest
import model.LoginResponse
import model.OwnerType
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

/**
 * Testes de integração HTTP para registo, login e associação professor-aluno.
 * Usam testApplication + H2 em memória.
 */
class AccountRoutesTest {

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
            this.install(ContentNegotiation) {
                json()
            }
        }
    }

    @Test
    fun `registar professor com sucesso devolve 201 e o id criado`() = testApplication {
        val client = jsonClient()

        val response = client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. Ana",
                    email = "ana@isel.pt",
                    password = "Teacher123"
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val body = response.body<TeacherResponse>()
        assertTrue(body.id > 0)
        assertEquals("Prof. Ana", body.name)
        assertEquals("ana@isel.pt", body.email)
    }

    @Test
    fun `registar professor com email repetido devolve 409 com mensagem amigavel`() = testApplication {
        val client = jsonClient()

        val first = client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. Ana",
                    email = "ana@isel.pt",
                    password = "Teacher123"
                )
            )
        }

        assertEquals(HttpStatusCode.Created, first.status)

        val second = client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. Ana 2",
                    email = "ana@isel.pt",
                    password = "Teacher123"
                )
            )
        }

        assertEquals(HttpStatusCode.Conflict, second.status)
        assertTrue(
            second.bodyAsText().contains("Este email já está registado"),
            "A resposta devia indicar claramente que o email já existe"
        )
    }

    @Test
    fun `registar aluno e associar a um professor`() = testApplication {
        val client = jsonClient()

        val teacher = client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. João",
                    email = "joao@isel.pt",
                    password = "Teacher123"
                )
            )
        }.body<TeacherResponse>()

        val student = client.post("/students") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRequest(
                    name = "Maria",
                    email = "maria@isel.pt",
                    password = "Student123",
                    teacherId = null
                )
            )
        }.body<StudentResponse>()

        val assignResponse = client.post("/students/assign-teacher") {
            contentType(ContentType.Application.Json)
            setBody(
                AssignTeacherRequest(
                    studentId = student.id,
                    teacherId = teacher.id
                )
            )
        }

        assertEquals(HttpStatusCode.OK, assignResponse.status)

        val studentsOfTeacher = client.get("/students/by-teacher/${teacher.id}")
            .body<List<StudentResponse>>()

        assertEquals(1, studentsOfTeacher.size)
        assertEquals(student.id, studentsOfTeacher.first().id)
        assertEquals(teacher.id, studentsOfTeacher.first().teacherId)
    }

    @Test
    fun `login com credenciais corretas devolve userId e ownerType`() = testApplication {
        val client = jsonClient()

        val createdTeacher = client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. Login",
                    email = "login@isel.pt",
                    password = "Teacher123"
                )
            )
        }.body<TeacherResponse>()

        val loginResponse = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequest(
                    email = "login@isel.pt",
                    password = "Teacher123"
                )
            )
        }

        assertEquals(HttpStatusCode.OK, loginResponse.status)

        val body = loginResponse.body<LoginResponse>()
        assertEquals(createdTeacher.id, body.userId)
        assertEquals(OwnerType.TEACHER, body.ownerType)
    }

    @Test
    fun `login com password errada e rejeitado com 401`() = testApplication {
        val client = jsonClient()

        client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. Erro",
                    email = "erro@isel.pt",
                    password = "Teacher123"
                )
            )
        }

        val loginResponse = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequest(
                    email = "erro@isel.pt",
                    password = "PasswordErrada"
                )
            )
        }

        assertEquals(HttpStatusCode.Unauthorized, loginResponse.status)
        assertTrue(
            loginResponse.bodyAsText().contains("Email ou password incorretos"),
            "A resposta devia indicar credenciais inválidas"
        )
    }
}