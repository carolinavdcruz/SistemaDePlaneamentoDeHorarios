package plugin

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import model.AvailabilityRequest
import model.GenerateLessonsRequest
import model.LessonResponse
import model.NotifyStudentsRequest
import model.NotifyStudentsResponse
import model.OwnerType
import model.RecurrenceType
import model.RestrictionsRequest
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

class LessonRoutesTest {

    @BeforeTest
    fun setup() {
        TestDatabase.reset()
    }

    private suspend fun seedTeacherAndStudent(client: io.ktor.client.HttpClient): Pair<TeacherResponse, StudentResponse> {
        val teacher = client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(TeacherRequest(name = "Prof. Ana", email = "ana@isel.pt", password = "1234"))
        }.body<TeacherResponse>()

        val student = client.post("/students") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRequest(
                    name = "Joao",
                    email = "joao@isel.pt",
                    password = "1234",
                    teacherId = teacher.id
                )
            )
        }.body<StudentResponse>()

        client.put("/restrictions/${teacher.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                RestrictionsRequest(
                    teacherId = teacher.id,
                    maxDailyHours = 8,
                    sessionDurationMinutes = 60,
                    maxParticipantsPerSession = 5,
                    maxSessionsPerStudentPerDay = 2
                )
            )
        }

        client.post("/availability") {
            contentType(ContentType.Application.Json)
            setBody(
                AvailabilityRequest(
                    ownerId = teacher.id,
                    ownerType = OwnerType.TEACHER,
                    dayOfWeek = 1,
                    startTime = "09:00",
                    endTime = "10:00"
                )
            )
        }

        client.post("/availability") {
            contentType(ContentType.Application.Json)
            setBody(
                AvailabilityRequest(
                    ownerId = student.id,
                    ownerType = OwnerType.STUDENT,
                    dayOfWeek = 1,
                    startTime = "09:00",
                    endTime = "10:00"
                )
            )
        }
        return teacher to student
    }

    private fun ApplicationTestBuilder.jsonClient(): io.ktor.client.HttpClient {
        application {
            configureSerialization()
            configureRouting()
        }
        return createClient { install(ContentNegotiation) { json() } }
    }

    @Test
    fun testeGerarAulasComDisponibilidadeCoincidente() = testApplication {
        TODO()
    }

    @Test
    fun testeGerarAulasSemRestricoesDevolve404() = testApplication {
        val client = jsonClient()
        val teacher = client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = "Prof. Sem Restricoes",
                    email = "semrestricoes@isel.pt",
                    password = "1234"
                )
            )
        }.body<TeacherResponse>()

        val response = client.post("/lessons/generate") {
            contentType(ContentType.Application.Json)
            setBody(GenerateLessonsRequest(teacherId = teacher.id, startDate = "2026-01-05"))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testeCancelarAulaMarcaCANCELLEDSemAfetarOutras() = testApplication {
        TODO()
    }

    @Test
    fun testeCancelarSerieCancelaTodasOcorrenciasScheduled() = testApplication {
        TODO()
    }

    @Test
    fun testeMarcarPresencaAtualizaEstadoEResumo() = testApplication {
        TODO()
    }

    @Test
    fun testeRemarcarAulaParaCimaDeOutraDevolveConflito409() = testApplication {
        TODO()
    }

    @Test
    fun testeEnviarAvisoManualATodosOsAlunos() = testApplication {
        val client = jsonClient()
        val (teacher, _) = seedTeacherAndStudent(client)

        val response = client.post("/teachers/${teacher.id}/notify") {
            contentType(ContentType.Application.Json)
            setBody(
                NotifyStudentsRequest(
                    studentIds = null,
                    subject = "Aviso de teste",
                    message = "Isto e uma mensagem de teste."
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<NotifyStudentsResponse>()
        assertEquals(1, body.sentTo)
    }

    @Test
    fun testeHistoricoSoDevolveAulasDentroDoIntervalo() = testApplication {
        TODO()
    }
}