package com.example.frontend.data.model.scheduling

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.frontend.data.model.TimeSlot
import java.time.LocalTime

/**
 * Converte um intervalo de disponibilidade em blocos de slotDurationMinutes em minutos.
 * Exemplo: 09:00–11:00 com 60 min → [09:00–10:00, 10:00–11:00]
 */
object TimeSlotProcessor {

    @RequiresApi(Build.VERSION_CODES.O)
    fun process(
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        slotDurationMinutes: Long // = 60L
    ): List<TimeSlot> {

        val start = LocalTime.parse(startTime)
        val end   = LocalTime.parse(endTime)

        if (!start.isBefore(end)) return emptyList()

        val slots = mutableListOf<TimeSlot>()

        var current = start

        // var slotId  = 0

        while (current.plusMinutes(slotDurationMinutes) <= end) {

            val next = current.plusMinutes(slotDurationMinutes)

            slots.add(
                TimeSlot(
                    //id         = slotId++,
                    dayOfWeek = dayOfWeek,
                    startTime = current,
                    endTime = next
                )
            )

            current = next

        }

        return slots

    }

    /**
     * Processa uma lista de disponibilidades (vários dias) de uma só vez.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun processAll(
        availabilities: List<Triple<Int, String, String>>, // (dayOfWeek, start, end)
        slotDurationMinutes: Long // = 60L
    ): List<TimeSlot> {

        return availabilities.flatMap { (day, start, end) ->

            process(
                day,
                start,
                end,
                slotDurationMinutes = slotDurationMinutes
            )

        }
    }
}