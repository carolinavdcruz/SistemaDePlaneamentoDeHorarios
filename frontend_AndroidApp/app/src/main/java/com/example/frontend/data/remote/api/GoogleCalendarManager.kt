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

class GoogleCalendarManager(private val context: Context) {

    companion object {
        // Scope necessário para criar eventos no Calendar
        val SCOPES = listOf(CalendarScopes.CALENDAR)
    }

    fun hasCalendarPermission(context: Context): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false
        return GoogleSignIn.hasPermissions(
            account,
            Scope(CalendarScopes.CALENDAR)
        )
    }

    // Cria as credenciais OAuth com a conta Google já autenticada via Google Sign-In
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

        val lessonDate = java.time.LocalDate.parse(lesson.date)
        val lessonStartTime = java.time.LocalTime.parse(lesson.startTime)
        val lessonEndTime = java.time.LocalTime.parse(lesson.endTime)

        val startDateTimeMillis = lessonDate.atTime(lessonStartTime)
            .atZone(zone.toZoneId())
            .toInstant()
            .toEpochMilli()

        val endDateTimeMillis = lessonDate.atTime(lessonEndTime)
            .atZone(zone.toZoneId())
            .toInstant()
            .toEpochMilli()

        val names = lesson.students
            .mapNotNull { student -> studentNames[student.studentId] }
            .joinToString(", ")

        val lessonKey = "lesson-${lesson.id}"

        return Event().apply {
            summary = if (names.isNotBlank()) {
                "Sessão: $names"
            } else {
                "Sessão"
            }

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

            start = EventDateTime()
                .setDateTime(DateTime(startDateTimeMillis))
                .setTimeZone(zone.id)

            end = EventDateTime()
                .setDateTime(DateTime(endDateTimeMillis))
                .setTimeZone(zone.id)
        }
    }

    private fun lessonAlreadyExists(
        service: Calendar,
        lesson: LessonResponse
    ): Boolean {
        val zone = java.util.TimeZone.getDefault()

        val lessonDate = java.time.LocalDate.parse(lesson.date)
        val lessonStartTime = java.time.LocalTime.parse(lesson.startTime)
        val lessonEndTime = java.time.LocalTime.parse(lesson.endTime)

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
