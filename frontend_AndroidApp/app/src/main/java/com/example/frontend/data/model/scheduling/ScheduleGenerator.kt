package com.example.frontend.data.model.scheduling

import com.example.frontend.data.model.Restrictions
import com.example.frontend.data.model.ScheduledSession
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.model.TimeSlot

class ScheduleGenerator {

    /**
     * GREEDY VERSION
     *
     * For each slot available for the teacher, assigns students that:
     *   1. are available in that slot (day + hour)
     *   2. don't have more than maxDailySessions in the same day
     *   3. don't have more than maxCapacityPerSession in the same session
     *
     * Order students by least availability (most restricted first).
     */
    fun create(
        teacherId: Int,
        teacherSlots: List<TimeSlot>,
        students: List<StudentEntity>,
        studentAvailabilities: Map<Int, List<TimeSlot>>,   // studentId - slots
        restrictions: Restrictions
    ): List<ScheduledSession> {

        val sessions = mutableListOf<ScheduledSession>()

        val studentSessionsPerDay = mutableMapOf<Int, MutableMap<Int, Int>>()
        val teacherMinutesPerDay = mutableMapOf<Int, Int>()

        /*
        // studentId - (dayOfWeek - count of sessions in that day)
        val dailyCount = mutableMapOf<Int, MutableMap<Int, Int>>()
         */

        // student with less availability is assigned first
        val sortedStudents = students.sortedBy { student ->
            studentAvailabilities[student.id]?.size ?: 0
        }

        for (slot in teacherSlots) {

            val minutesUsedToday = teacherMinutesPerDay.getOrDefault(slot.dayOfWeek, 0)

            if (minutesUsedToday + restrictions.sessionDurationMinutes > restrictions.maxDailyHours * 60) {
                continue
            }

            val enrolled = mutableListOf<Int>()

            for (student in sortedStudents) {
                if (enrolled.size >= restrictions.maxParticipantsPerSession) break

                val studentSlots = studentAvailabilities[student.id] ?: continue

                // Student available in that slot ?
                val available = studentSlots.any {
                            it.dayOfWeek == slot.dayOfWeek &&
                            it.startTime == slot.startTime &&
                            it.endTime == slot.endTime
                }

                if (!available) continue

                // have more than maxDailySessions in the same day
                val studentDayMap = studentSessionsPerDay.getOrPut(student.id) { mutableMapOf() }
                val countSessionsToday = studentDayMap.getOrDefault(slot.dayOfWeek, 0)

                if(countSessionsToday >= restrictions.maxSessionsPerStudentPerDay) {
                    continue
                }

                // assign to session
                enrolled.add(student.id)
                studentDayMap[slot.dayOfWeek] = countSessionsToday + 1
            }

            if (enrolled.isNotEmpty()) {
                sessions.add(
                    ScheduledSession(
                        teacherId = teacherId,
                        studentIds = enrolled,
                        dayOfWeek = slot.dayOfWeek,
                        startTime = slot.startTime.toString(),
                        endTime = slot.endTime.toString()
                    )
                )
                teacherMinutesPerDay[slot.dayOfWeek] = minutesUsedToday + restrictions.sessionDurationMinutes
            }
        }
        return sessions
    }
}