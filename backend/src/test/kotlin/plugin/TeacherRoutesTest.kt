package plugin

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import model.TeacherRequest
import model.TeacherResponse
import plugins.configureRouting
import plugins.configureSerialization
import testsuport.TestDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TeacherRoutesTest {

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
    fun `get teachers devolve lista vazia inicialmente`() = testApplication {
        val client = jsonClient()

        val response = client.get("/teachers")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(emptyList<TeacherResponse>(), response.body())
    }

    @Test
    fun `get teachers devolve professores criados`() = testApplication {
        val client = jsonClient()

        client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. Ana",
                    email = "ana-list@isel.pt",
                    password = "Teacher123"
                )
            )
        }

        client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. Pedro",
                    email = "pedro-list@isel.pt",
                    password = "Teacher123"
                )
            )
        }

        val response = client.get("/teachers")

        assertEquals(HttpStatusCode.OK, response.status)

        val teachers = response.body<List<TeacherResponse>>()
        assertEquals(2, teachers.size)
    }
}