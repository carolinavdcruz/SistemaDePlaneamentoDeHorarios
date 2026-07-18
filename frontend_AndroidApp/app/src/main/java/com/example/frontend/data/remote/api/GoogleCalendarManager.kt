package com.example.frontend.data.remote.api

import android.content.Context
import com.example.frontend.data.remote.dto.LessonResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class GoogleCalendarManager(private val context: Context) {

    companion object {
        val SCOPES = listOf(CalendarScopes.CALENDAR)
    }

    fun hasCalendarPermission(context: Context): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false
        return GoogleSignIn.hasPermissions(
            account,
            Scope(CalendarScopes.CALENDAR)
        )
    }

    private fun buildCalendarService(): Calendar? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null

        val credential = GoogleAccountCredential
            .usingOAuth2(context, SCOPES)
            .setBackOff(ExponentialBackOff())
            .also { it.selectedAccount = account.account }

        return Calendar.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("SistemaDePlaneamento").build()
    }

    suspend fun addLessonsToCalendar(
        lessons: List<LessonResponse>,
        studentNames: Map<Int, String>
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val service = buildCalendarService()
                ?: return@withContext Result.failure(Exception("Não autenticado no Google"))

            var count = 0
            lessons.forEach { lesson ->
                if (lesson.status != "CANCELLED" && !lessonAlreadyExists(service, lesson)) {
                    val event = buildLessonEvent(lesson, studentNames)
                    service.events().insert("primary", event).execute()
                    count++
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildLessonEvent(
        lesson: LessonResponse,
        studentNames: Map<Int, String>
    ): Event {
        val zone = java.util.TimeZone.getDefault()

        val lessonDate = LocalDate.parse(lesson.date)
        val lessonStartTime = LocalTime.parse(lesson.startTime)
        val lessonEndTime = LocalTime.parse(lesson.endTime)

        // Converter para string RFC 3339 com offset explícito.
        // Assim o DateTime não é tratado como UTC e não há double-offset.
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val startRfc = lessonDate.atTime(lessonStartTime)
            .atZone(zone.toZoneId())
            .format(formatter)
        val endRfc = lessonDate.atTime(lessonEndTime)
            .atZone(zone.toZoneId())
            .format(formatter)

        val names = lesson.students
            .mapNotNull { student -> studentNames[student.studentId] }
            .joinToString(", ")

        val lessonKey = "lesson-${lesson.id}"

        return Event().apply {
            summary = if (names.isNotBlank()) "Sessão: $names" else "Sessão"

            description = buildString {
                append("Aula gravada no Sistema de Planeamento de Horários.")
                append("\nLessonId: ${lesson.id}")
                append("\nData: ${lesson.date}")
                append("\nHora: ${lesson.startTime} - ${lesson.endTime}")
                if (lesson.seriesId != null) {
                    append("\nSérie: ${lesson.seriesId}")
                }
            }

            extendedProperties = Event.ExtendedProperties().setPrivate(
                mapOf("sphLessonId" to lessonKey)
            )

            // Sem setTimeZone: o offset já está embutido na string RFC 3339,
            // por isso o Google Calendar não aplica nenhuma conversão extra.
            start = EventDateTime().setDateTime(DateTime(startRfc))
            end = EventDateTime().setDateTime(DateTime(endRfc))
        }
    }

    private fun lessonAlreadyExists(
        service: Calendar,
        lesson: LessonResponse
    ): Boolean {
        val zone = java.util.TimeZone.getDefault()

        val lessonDate = LocalDate.parse(lesson.date)
        val lessonStartTime = LocalTime.parse(lesson.startTime)
        val lessonEndTime = LocalTime.parse(lesson.endTime)

        val startMillis = lessonDate.atTime(lessonStartTime)
            .atZone(zone.toZoneId())
            .toInstant()
            .toEpochMilli()

        val endMillis = lessonDate.atTime(lessonEndTime)
            .atZone(zone.toZoneId())
            .toInstant()
            .toEpochMilli()

        val expectedKey = "lesson-${lesson.id}"

        val events = service.events().list("primary")
            .setTimeMin(DateTime(startMillis))
            .setTimeMax(DateTime(endMillis))
            .setSingleEvents(true)
            .execute()

        return events.items.orEmpty().any { event ->
            val privateProps = event.extendedProperties?.getPrivate()
            privateProps?.get("sphLessonId") == expectedKey
        }
    }
}