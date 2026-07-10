package plugin

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import model.LoginRequest
import model.LoginResponse
import model.OwnerType
import model.StudentRequest
import model.StudentResponse
import model.TeacherRequest
import plugins.configureRouting
import plugins.configureSerialization
import testsuport.TestDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesExtraTest {

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
    fun `login de aluno com sucesso devolve ownerType STUDENT`() = testApplication {
        val client = jsonClient()

        val student = client.post("/students") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRequest(
                    name = "Aluno Login",
                    email = "student-login@isel.pt",
                    password = "Student123"
                )
            )
        }.body<StudentResponse>()

        val loginResponse = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequest(
                    email = "student-login@isel.pt",
                    password = "Student123"
                )
            )
        }

        assertEquals(HttpStatusCode.OK, loginResponse.status)

        val body = loginResponse.body<LoginResponse>()
        assertEquals(student.id, body.userId)
        assertEquals(OwnerType.STUDENT, body.ownerType)
    }

    @Test
    fun `login com email inexistente devolve 401`() = testApplication {
        val client = jsonClient()

        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequest(
                    email = "naoexiste@isel.pt",
                    password = "Qualquer123"
                )
            )
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertTrue(response.bodyAsText().contains("Email ou password incorretos"))
    }

    @Test
    fun `login com password errada para aluno devolve 401`() = testApplication {
        val client = jsonClient()

        client.post("/students") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRequest(
                    name = "Aluno Erro",
                    email = "student-error@isel.pt",
                    password = "Student123"
                )
            )
        }

        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequest(
                    email = "student-error@isel.pt",
                    password = "Errada123"
                )
            )
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertTrue(response.bodyAsText().contains("Email ou password incorretos"))
    }

    @Test
    fun `login de professor continua a devolver ownerType TEACHER`() = testApplication {
        val client = jsonClient()

        client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. Auth",
                    email = "prof-auth@isel.pt",
                    password = "Teacher123"
                )
            )
        }

        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequest(
                    email = "prof-auth@isel.pt",
                    password = "Teacher123"
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<LoginResponse>()
        assertEquals(OwnerType.TEACHER, body.ownerType)
    }
}