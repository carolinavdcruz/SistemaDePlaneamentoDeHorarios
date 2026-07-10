package plugin

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import model.AttendanceSummaryResponse
import model.AvailabilityRequest
import model.CancelSeriesResponse
import model.GenerateLessonsRequest
import model.LessonConflictResponse
import model.LessonResponse
import model.LoginRequest
import model.MarkAttendanceRequest
import model.NotifyStudentsRequest
import model.NotifyStudentsResponse
import model.OwnerType
import model.RecurrenceType
import model.RestrictionsRequest
import model.StudentRequest
import model.StudentResponse
import model.TeacherRequest
import model.TeacherResponse
import model.UpdateLessonRequest
import plugins.configureRouting
import plugins.configureSerialization
import testsuport.TestDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LessonRoutesTest {

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

    private suspend fun createTeacher(
        client: HttpClient,
        name: String = "Prof. Ana",
        email: String = "ana@isel.pt"
    ): TeacherResponse {
        return client.post("/teachers") {
            contentType(ContentType.Application.Json)
            setBody(
                TeacherRequest(
                    name = name,
                    email = email,
                    password = "Teacher123"
                )
            )
        }.body()
    }

    private suspend fun createStudent(
        client: HttpClient,
        name: String,
        email: String,
        teacherId: Int
    ): StudentResponse {
        return client.post("/students") {
            contentType(ContentType.Application.Json)
            setBody(
                StudentRequest(
                    name = name,
                    email = email,
                    password = "Student123",
                    teacherId = teacherId
                )
            )
        }.body()
    }

    private suspend fun saveRestrictions(
        client: HttpClient,
        teacherId: Int,
        maxDailyHours: Int = 8,
        sessionDurationMinutes: Int = 60,
        maxParticipantsPerSession: Int = 5,
        maxSessionsPerStudentPerDay: Int = 2
    ) {
        client.put("/restrictions/$teacherId") {
            contentType(ContentType.Application.Json)
            setBody(
                RestrictionsRequest(
                    teacherId = teacherId,
                    maxDailyHours = maxDailyHours,
                    sessionDurationMinutes = sessionDurationMinutes,
                    maxParticipantsPerSession = maxParticipantsPerSession,
                    maxSessionsPerStudentPerDay = maxSessionsPerStudentPerDay
                )
            )
        }
    }

    private suspend fun addAvailability(
        client: HttpClient,
        ownerId: Int,
        ownerType: OwnerType,
        dayOfWeek: Int,
        startTime: String,
        endTime: String
    ) {
        client.post("/availability") {
            contentType(ContentType.Application.Json)
            setBody(
                AvailabilityRequest(
                    ownerId = ownerId,
                    ownerType = ownerType,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime
                )
            )
        }
    }

    private suspend fun seedTeacherAndStudent(client: HttpClient): Pair<TeacherResponse, StudentResponse> {
        val teacher = createTeacher(client)
        val student = createStudent(
            client = client,
            name = "Joao",
            email = "joao@isel.pt",
            teacherId = teacher.id
        )

        saveRestrictions(client, teacher.id)

        addAvailability(
            client = client,
            ownerId = teacher.id,
            ownerType = OwnerType.TEACHER,
            dayOfWeek = 1,
            startTime = "09:00",
            endTime = "10:00"
        )

        addAvailability(
            client = client,
            ownerId = student.id,
            ownerType = OwnerType.STUDENT,
            dayOfWeek = 1,
            startTime = "09:00",
            endTime = "10:00"
        )

        return teacher to student
    }

    @Test
    fun testeGerarAulasComDisponibilidadeCoincidente() = testApplication {
        val client = jsonClient()
        val (teacher, student) = seedTeacherAndStudent(client)

        val response = client.post("/lessons/generate") {
            contentType(ContentType.Application.Json)
            setBody(
                GenerateLessonsRequest(
                    teacherId = teacher.id,
                    startDate = "2026-01-05",
                    recurrence = RecurrenceType.NONE,
                    occurrences = 1
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val lessons = response.body<List<LessonResponse>>()
        assertEquals(1, lessons.size)
        assertEquals("2026-01-05", lessons.first().date)
        assertEquals("09:00", lessons.first().startTime)
        assertEquals("10:00", lessons.first().endTime)
        assertEquals(listOf(student.id), lessons.first().students.map { it.studentId })
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
        val client = jsonClient()
        val (teacher, _) = seedTeacherAndStudent(client)

        val generated = client.post("/lessons/generate") {
            contentType(ContentType.Application.Json)
            setBody(
                GenerateLessonsRequest(
                    teacherId = teacher.id,
                    startDate = "2026-01-05",
                    recurrence = RecurrenceType.WEEKLY,
                    occurrences = 2
                )
            )
        }.body<List<LessonResponse>>()

        assertEquals(2, generated.size)

        val cancelResponse = client.patch("/lessons/${generated[0].id}/cancel")
        assertEquals(HttpStatusCode.OK, cancelResponse.status)

        val first = client.get("/lessons/${generated[0].id}").body<LessonResponse>()
        val second = client.get("/lessons/${generated[1].id}").body<LessonResponse>()

        assertEquals("CANCELLED", first.status)
        assertEquals("SCHEDULED", second.status)
    }

    @Test
    fun testeCancelarSerieCancelaTodasOcorrenciasScheduled() = testApplication {
        val client = jsonClient()
        val (teacher, _) = seedTeacherAndStudent(client)

        val generated = client.post("/lessons/generate") {
            contentType(ContentType.Application.Json)
            setBody(
                GenerateLessonsRequest(
                    teacherId = teacher.id,
                    startDate = "2026-01-05",
                    recurrence = RecurrenceType.WEEKLY,
                    occurrences = 3
                )
            )
        }.body<List<LessonResponse>>()

        val seriesId = generated.first().seriesId
        assertNotNull(seriesId)

        val deleteResponse = client.delete("/lessons/series/$seriesId")
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        val body = deleteResponse.body<CancelSeriesResponse>()
        assertEquals(3, body.cancelledCount)

        generated.forEach { lesson ->
            val fetched = client.get("/lessons/${lesson.id}").body<LessonResponse>()
            assertEquals("CANCELLED", fetched.status)
        }
    }

    @Test
    fun testeMarcarPresencaAtualizaEstadoEResumo() = testApplication {
        val client = jsonClient()
        val (teacher, student) = seedTeacherAndStudent(client)

        val generated = client.post("/lessons/generate") {
            contentType(ContentType.Application.Json)
            setBody(
                GenerateLessonsRequest(
                    teacherId = teacher.id,
                    startDate = "2026-01-05",
                    recurrence = RecurrenceType.WEEKLY,
                    occurrences = 3
                )
            )
        }.body<List<LessonResponse>>()

        val markPresent = client.patch("/lessons/${generated[0].id}/students/${student.id}/attendance") {
            contentType(ContentType.Application.Json)
            setBody(MarkAttendanceRequest(attended = true))
        }
        assertEquals(HttpStatusCode.OK, markPresent.status)

        val markAbsent = client.patch("/lessons/${generated[1].id}/students/${student.id}/attendance") {
            contentType(ContentType.Application.Json)
            setBody(MarkAttendanceRequest(attended = false))
        }
        assertEquals(HttpStatusCode.OK, markAbsent.status)

        val summary = client.get("/lessons/students/${student.id}/attendance-summary")
            .body<AttendanceSummaryResponse>()

        assertEquals(student.id, summary.studentId)
        assertEquals(3, summary.totalLessons)
        assertEquals(1, summary.attended)
        assertEquals(1, summary.missed)
        assertEquals(1, summary.pending)
        assertEquals(0.5, summary.attendanceRate)
    }

    @Test
    fun testeRemarcarAulaParaCimaDeOutraDevolveConflito409() = testApplication {
        val client = jsonClient()
        val teacher = createTeacher(client, email = "conflito@isel.pt")
        val student = createStudent(client, "Maria", "maria@isel.pt", teacher.id)

        saveRestrictions(
            client = client,
            teacherId = teacher.id,
            maxDailyHours = 8,
            sessionDurationMinutes = 60,
            maxParticipantsPerSession = 5,
            maxSessionsPerStudentPerDay = 5
        )

        addAvailability(client, teacher.id, OwnerType.TEACHER, 1, "09:00", "11:00")
        addAvailability(client, student.id, OwnerType.STUDENT, 1, "09:00", "11:00")

        val generated = client.post("/lessons/generate") {
            contentType(ContentType.Application.Json)
            setBody(
                GenerateLessonsRequest(
                    teacherId = teacher.id,
                    startDate = "2026-01-05",
                    recurrence = RecurrenceType.NONE,
                    occurrences = 1
                )
            )
        }.body<List<LessonResponse>>()

        assertEquals(2, generated.size)

        val firstLesson = generated.first { it.startTime == "09:00" }
        val secondLesson = generated.first { it.startTime == "10:00" }

        val updateResponse = client.patch("/lessons/${firstLesson.id}") {
            contentType(ContentType.Application.Json)
            setBody(
                UpdateLessonRequest(
                    date = "2026-01-05",
                    startTime = "09:30",
                    endTime = "10:30"
                )
            )
        }

        assertEquals(HttpStatusCode.Conflict, updateResponse.status)

        val body = updateResponse.body<LessonConflictResponse>()
        assertEquals(secondLesson.id, body.conflictingLessonId)
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
        val client = jsonClient()
        val (teacher, _) = seedTeacherAndStudent(client)

        client.post("/lessons/generate") {
            contentType(ContentType.Application.Json)
            setBody(
                GenerateLessonsRequest(
                    teacherId = teacher.id,
                    startDate = "2026-01-05",
                    recurrence = RecurrenceType.WEEKLY,
                    occurrences = 4
                )
            )
        }

        val historyResponse = client.get("/lessons/history") {
            parameter("teacherId", teacher.id)
            parameter("from", "2026-01-05")
            parameter("to", "2026-01-18")
        }

        assertEquals(HttpStatusCode.OK, historyResponse.status)

        val history = historyResponse.body<List<LessonResponse>>()
        assertEquals(2, history.size)
        assertTrue(history.all { it.date == "2026-01-05" || it.date == "2026-01-12" })
    }
}