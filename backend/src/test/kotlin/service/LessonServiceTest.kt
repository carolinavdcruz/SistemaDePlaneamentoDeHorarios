package service

import database.tables.LessonStatus
import database.tables.StudentTable
import database.tables.TeacherTable
import model.Session
import model.TimeSlot
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import testsuport.TestDatabase
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Testes de LessonService usando uma base de dados H2 em memória — não precisa
 * do Postgres a correr. A BD é partilhada com os restantes testes (ver
 * testsupport.TestDatabase) e limpa antes de cada teste, para cada teste
 * começar com dados conhecidos e isolados dos outros.
 */
class LessonServiceTest {

    private var teacherId: Int = 0
    private var otherTeacherId: Int = 0
    private var student1Id: Int = 0
    private var student2Id: Int = 0

    // Segunda-feira conhecida, para os testes de recorrência/semana ficarem previsíveis.
    private val monday = LocalDate.of(2026, 1, 5)

    @BeforeTest
    fun setup() {
        TestDatabase.reset()
        transaction(TestDatabase.db) {
            teacherId = TeacherTable.insert {
                it[name] = "Prof. Ana"
                it[email] = "ana@isel.pt"
                it[password] = "hash"
            }[TeacherTable.id].value

            otherTeacherId = TeacherTable.insert {
                it[name] = "Prof. Bruno"
                it[email] = "bruno@isel.pt"
                it[password] = "hash"
            }[TeacherTable.id].value

            student1Id = StudentTable.insert {
                it[name] = "João"
                it[email] = "joao@isel.pt"
                it[password] = "hash"
                it[StudentTable.teacherId] = teacherId
            }[StudentTable.id].value

            student2Id = StudentTable.insert {
                it[name] = "Maria"
                it[email] = "maria@isel.pt"
                it[password] = "hash"
                it[StudentTable.teacherId] = teacherId
            }[StudentTable.id].value
        }
    }

    private fun session(dayOfWeek: Int, start: LocalTime, end: LocalTime, vararg studentIds: Int) =
        Session(
            slot = TimeSlot(dayOfWeek = dayOfWeek, startTime = start, endTime = end),
            studentIds = studentIds.toList()
        )

    //  persistRecurring

    @Test
    fun `persistRecurring cria uma aula por semana por sessao`() {
        val seg9h = session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id)

        val lessons = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(seg9h),
            startDate = monday,
            occurrences = 3
        )

        assertEquals(3, lessons.size)
        assertEquals(monday, lessons[0].date)
        assertEquals(monday.plusWeeks(1), lessons[1].date)
        assertEquals(monday.plusWeeks(2), lessons[2].date)
        lessons.forEach {
            assertEquals(LessonStatus.SCHEDULED, it.status)
            assertEquals(listOf(student1Id), it.students.map { s -> s.studentId })
        }
    }

    @Test
    fun `persistRecurring com 1 ocorrencia nao atribui seriesId`() {
        val seg9h = session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id)

        val lessons = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(seg9h),
            startDate = monday,
            occurrences = 1
        )

        assertEquals(1, lessons.size)
        assertNull(lessons[0].seriesId, "Aula avulsa não devia ter seriesId")
    }

    @Test
    fun `persistRecurring com mais de 1 ocorrencia atribui o mesmo seriesId a todas`() {
        val seg9h = session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id)

        val lessons = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(seg9h),
            startDate = monday,
            occurrences = 4
        )

        val seriesIds = lessons.map { it.seriesId }.distinct()
        assertEquals(1, seriesIds.size, "Todas as ocorrências deviam partilhar o mesmo seriesId")
        assertNotNull(seriesIds.first())
    }

    @Test
    fun `dayOfWeek 3 (quarta) soma o deslocamento certo a partir da segunda`() {
        val quarta10h = session(3, LocalTime.of(10, 0), LocalTime.of(11, 0), student1Id)

        val lessons = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(quarta10h),
            startDate = monday,
            occurrences = 1
        )

        assertEquals(
            monday.plusDays(2),
            lessons[0].date,
            "dayOfWeek=3 devia cair 2 dias depois da segunda"
        )
    }

    //  getById / getHistory

    @Test
    fun `getById devolve null para aula inexistente`() {
        assertNull(LessonService.getById(9999))
    }

    @Test
    fun `getById devolve a aula com os alunos corretos`() {
        val created = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id, student2Id)),
            startDate = monday,
            occurrences = 1
        ).first()

        val fetched = LessonService.getById(created.id)

        assertNotNull(fetched)
        assertEquals(setOf(student1Id, student2Id), fetched.students.map { it.studentId }.toSet())
    }

    @Test
    fun `getHistory so devolve aulas dentro do intervalo de datas`() {
        LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id)),
            startDate = monday,
            occurrences = 4 // 4 semanas seguidas
        )

        // só a 1ª e 2ª semana
        val history = LessonService.getHistory(teacherId, monday, monday.plusWeeks(1).plusDays(6))

        assertEquals(2, history.size)
    }

    //  markAttendance / getAttendanceSummary

    @Test
    fun `markAttendance marca presenca e regista attendedAt`() {
        val lesson = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id)),
            startDate = monday,
            occurrences = 1
        ).first()

        val updated = LessonService.markAttendance(lesson.id, student1Id, true)
        assertTrue(updated)

        val fetched = LessonService.getById(lesson.id)!!
        val studentInfo = fetched.students.single { it.studentId == student1Id }
        assertEquals(true, studentInfo.attended)
        assertNotNull(studentInfo.attendedAt)
    }

    @Test
    fun `markAttendance devolve false se o par aula-aluno nao existir`() {
        val lesson = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id)),
            startDate = monday,
            occurrences = 1
        ).first()

        // student2Id não está inscrito nesta aula
        val updated = LessonService.markAttendance(lesson.id, student2Id, true)
        assertFalse(updated)
    }

    @Test
    fun `getAttendanceSummary calcula presentes, faltas, pendentes e taxa corretamente`() {
        // 3 aulas para o mesmo aluno: 1 presente, 1 falta, 1 por marcar
        val lessons = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id)),
            startDate = monday,
            occurrences = 3
        )

        LessonService.markAttendance(lessons[0].id, student1Id, true)
        LessonService.markAttendance(lessons[1].id, student1Id, false)
        // lessons[2] fica por marcar (pending)

        val summary = LessonService.getAttendanceSummary(student1Id)

        assertEquals(3, summary.totalLessons)
        assertEquals(1, summary.attended)
        assertEquals(1, summary.missed)
        assertEquals(1, summary.pending)
        assertEquals(0.5, summary.attendanceRate, "Taxa = presentes / (presentes+faltas) = 1/2")
    }

    //  cancelLesson / cancelSeries

    @Test
    fun `cancelLesson devolve false para aula inexistente`() {
        assertFalse(LessonService.cancelLesson(9999))
    }

    @Test
    fun `cancelLesson marca apenas essa aula como CANCELLED`() {
        val lessons = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id)),
            startDate = monday,
            occurrences = 2
        )

        val cancelled = LessonService.cancelLesson(lessons[0].id)
        assertTrue(cancelled)

        assertEquals(LessonStatus.CANCELLED, LessonService.getById(lessons[0].id)!!.status)
        assertEquals(
            LessonStatus.SCHEDULED,
            LessonService.getById(lessons[1].id)!!.status,
            "A outra aula não devia ser afetada"
        )
    }

    @Test
    fun `cancelSeries cancela todas as aulas SCHEDULED da serie e devolve a contagem`() {
        val lessons = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id)),
            startDate = monday,
            occurrences = 3
        )
        val seriesId = lessons[0].seriesId!!

        // uma das aulas já estava cancelada manualmente antes
        LessonService.cancelLesson(lessons[0].id)

        val affected = LessonService.cancelSeries(seriesId)

        assertEquals(2, affected, "Só as 2 que ainda estavam SCHEDULED deviam ser contadas")
        lessons.forEach {
            assertEquals(LessonStatus.CANCELLED, LessonService.getById(it.id)!!.status)
        }
    }

    //  updateLesson

    @Test
    fun `updateLesson devolve NotFound para aula inexistente`() {
        val result = LessonService.updateLesson(9999, null, null, null)
        assertTrue(result is LessonService.UpdateLessonResult.NotFound)
    }

    @Test
    fun `updateLesson muda data-hora e destaca a aula da serie`() {
        val lesson = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id)),
            startDate = monday,
            occurrences = 2 // > 1 para gerar seriesId
        ).first()
        assertNotNull(lesson.seriesId)

        val result = LessonService.updateLesson(
            lessonId = lesson.id,
            date = monday.plusDays(1),
            startTime = LocalTime.of(14, 0),
            endTime = LocalTime.of(15, 0)
        )

        assertTrue(result is LessonService.UpdateLessonResult.Success)
        val updated = (result as LessonService.UpdateLessonResult.Success).lesson
        assertEquals(monday.plusDays(1), updated.date)
        assertEquals(LocalTime.of(14, 0), updated.startTime)
        assertNull(updated.seriesId, "Ao editar, a aula devia sair da série")
    }

    @Test
    fun `updateLesson deteta conflito com outra aula do mesmo professor`() {
        val lessonA = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id)),
            startDate = monday,
            occurrences = 1
        ).first()

        val lessonB = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(session(2, LocalTime.of(15, 0), LocalTime.of(16, 0), student2Id)),
            startDate = monday,
            occurrences = 1
        ).first()

        // tenta mover a aula B para cima da aula A (mesmo professor, mesmo dia/hora)
        val result = LessonService.updateLesson(
            lessonId = lessonB.id,
            date = lessonA.date,
            startTime = lessonA.startTime,
            endTime = lessonA.endTime
        )

        assertTrue(result is LessonService.UpdateLessonResult.Conflict)
        assertEquals(
            lessonA.id,
            (result as LessonService.UpdateLessonResult.Conflict).conflictingLessonId
        )
    }

    @Test
    fun `updateLesson nao gera conflito consigo propria`() {
        val lesson = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id)),
            startDate = monday,
            occurrences = 1
        ).first()

        // só muda a hora de fim, mantém o resto — não se devia comparar consigo própria
        val result = LessonService.updateLesson(
            lessonId = lesson.id,
            date = null,
            startTime = null,
            endTime = LocalTime.of(10, 30)
        )

        assertTrue(result is LessonService.UpdateLessonResult.Success)
    }

    @Test
    fun `updateLesson ignora aulas CANCELLED na deteçao de conflitos`() {
        val lessonA = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id)),
            startDate = monday,
            occurrences = 1
        ).first()
        LessonService.cancelLesson(lessonA.id)

        val lessonB = LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(session(2, LocalTime.of(15, 0), LocalTime.of(16, 0), student2Id)),
            startDate = monday,
            occurrences = 1
        ).first()

        val result = LessonService.updateLesson(
            lessonId = lessonB.id,
            date = lessonA.date,
            startTime = lessonA.startTime,
            endTime = lessonA.endTime
        )

        assertTrue(
            result is LessonService.UpdateLessonResult.Success,
            "Aula CANCELLED não devia bloquear o horário"
        )
    }

    //  getWeekForStudent / getHistoryForStudent

    @Test
    fun `getWeekForStudent so devolve aulas em que o aluno participa`() {
        LessonService.persistRecurring(
            teacherId = teacherId,
            sessions = listOf(
                session(1, LocalTime.of(9, 0), LocalTime.of(10, 0), student1Id),
                session(1, LocalTime.of(11, 0), LocalTime.of(12, 0), student2Id)
            ),
            startDate = monday,
            occurrences = 1
        )

        val week = LessonService.getWeekForStudent(student1Id, monday)

        assertEquals(1, week.size)
        assertEquals(listOf(student1Id), week[0].students.map { it.studentId })
    }
}