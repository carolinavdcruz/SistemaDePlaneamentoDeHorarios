package com.example.frontend.data.remote.api

import android.content.Context
import com.example.frontend.data.model.ScheduledSession
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
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

    // Cria as credenciais OAuth com a conta Google já autenticada via Google Sign-In
    private fun buildCalendarService(): Calendar? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null

        val credential = GoogleAccountCredential
            .usingOAuth2(context, SCOPES)
            .setBackOff(ExponentialBackOff())
            .also { it.selectedAccountName = account.email }

        return Calendar.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("SistemaDePlaneamento").build()
    }

    // Insere todas as sessões geradas no Google Calendar do professor
    suspend fun addSessionsToCalendar(
        sessions: List<ScheduledSession>,
        studentNames: Map<Int, String>
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val service = buildCalendarService()
                ?: return@withContext Result.failure(Exception("Não autenticado no Google"))

            var count = 0
            sessions.forEach { session ->
                val event = buildEvent(session, studentNames)
                service.events().insert("primary", event).execute()
                count++
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildEvent(
        session: ScheduledSession,
        studentNames: Map<Int, String>
    ): Event {
        val zone = java.util.TimeZone.getDefault()
        val cal  = java.util.Calendar.getInstance(zone)

        // Mapeia dayOfWeek (1=Seg … 7=Dom) para constante do java.util.Calendar
        val calDay = when (session.dayOfWeek) {
            1 -> java.util.Calendar.MONDAY
            2 -> java.util.Calendar.TUESDAY
            3 -> java.util.Calendar.WEDNESDAY
            4 -> java.util.Calendar.THURSDAY
            5 -> java.util.Calendar.FRIDAY
            6 -> java.util.Calendar.SATURDAY
            else -> java.util.Calendar.SUNDAY
        }

        // Avança até à próxima ocorrência desse dia
        while (cal.get(java.util.Calendar.DAY_OF_WEEK) != calDay) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        val (startH, startM) = session.startTime.split(":").map { it.toInt() }
        val (endH,   endM)   = session.endTime.split(":").map { it.toInt() }

        cal.set(java.util.Calendar.HOUR_OF_DAY, startH)
        cal.set(java.util.Calendar.MINUTE, startM)
        cal.set(java.util.Calendar.SECOND, 0)
        val startMs = cal.timeInMillis

        cal.set(java.util.Calendar.HOUR_OF_DAY, endH)
        cal.set(java.util.Calendar.MINUTE, endM)
        val endMs = cal.timeInMillis

        val names = session.studentIds.mapNotNull { studentNames[it] }.joinToString(", ")

        return Event().apply {
            summary     = "Sessão: $names"
            description = "Sessão gerada automaticamente pelo Sistema de Planeamento de Horários."
            start = EventDateTime()
                .setDateTime(com.google.api.client.util.DateTime(startMs))
                .setTimeZone(zone.id)
            end = EventDateTime()
                .setDateTime(com.google.api.client.util.DateTime(endMs))
                .setTimeZone(zone.id)
        }
    }
}