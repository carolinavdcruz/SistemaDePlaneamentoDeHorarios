package service

import model.Restrictions
import model.Session
import model.Student
import model.TimeSlot

object ScheduleService {

    fun create(
        teacherSlots: List<TimeSlot>,
        students: List<Student>,
        studentAvailabilities: Map<Int, List<TimeSlot>>,
        studentWeeklyHours: Map<Int, Int>,
        restrictions: Restrictions
    ): List<Session> {

        val sessions = mutableListOf<Session>()
        val studentSessionsPerDay = mutableMapOf<Int, MutableMap<Int, Int>>()
        val teacherMinutesPerDay = mutableMapOf<Int, Int>()
        val studentWeeklyMinutes = mutableMapOf<Int, Int>()


        // aluno com menos disponibilidade é atribuído primeiro
        val sortedStudents = students.sortedBy { student ->
            studentAvailabilities[student.id]?.size ?: 0
        }

        for (slot in teacherSlots) {
            // verifica se o professor ainda tem horas disponíveis no dia
            val minutesUsedToday = teacherMinutesPerDay.getOrDefault(slot.dayOfWeek, 0)
            if (minutesUsedToday + restrictions.sessionDurationMinutes > restrictions.maxDailyHours * 60) {
                continue
            }

            val enrolled = mutableListOf<Int>()

            for (student in sortedStudents) {
                if (enrolled.size >= restrictions.maxParticipantsPerSession) break

                val studentSlots = studentAvailabilities[student.id] ?: continue

                // aluno disponível neste slot?
                val available = studentSlots.any {
                    it.dayOfWeek == slot.dayOfWeek &&
                            it.startTime == slot.startTime &&
                            it.endTime == slot.endTime
                }
                if (!available) continue

                // aluno já atingiu o máximo de sessões no dia?
                val studentDayMap = studentSessionsPerDay.getOrPut(student.id) { mutableMapOf() }
                val countToday = studentDayMap.getOrDefault(slot.dayOfWeek, 0)
                if (countToday >= restrictions.maxSessionsPerStudentPerDay) continue

                val weeklyHours = studentWeeklyHours[student.id] ?: 3
                val weeklyMinutes = weeklyHours * 60
                val weeklyMinutesAlreadyUsed = studentWeeklyMinutes.getOrDefault(student.id, 0)

                if (weeklyMinutesAlreadyUsed + restrictions.sessionDurationMinutes > weeklyMinutes) continue

                // atribui
                enrolled.add(student.id)
                studentDayMap[slot.dayOfWeek] = countToday + 1
                studentWeeklyMinutes[student.id] = weeklyMinutesAlreadyUsed + restrictions.sessionDurationMinutes

            }

            if (enrolled.isNotEmpty()) {
                sessions.add(Session(slot = slot, studentIds = enrolled))
                teacherMinutesPerDay[slot.dayOfWeek] = minutesUsedToday + restrictions.sessionDurationMinutes
            }
        }
        return sessions
    }
}