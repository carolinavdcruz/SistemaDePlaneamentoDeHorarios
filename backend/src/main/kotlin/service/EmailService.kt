package service

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalTime
import java.util.Properties
import java.util.concurrent.Executors

/**
 * Configuração SMTP lida de variáveis de ambiente. Para usar Gmail:
 *   SMTP_HOST=smtp.gmail.com
 *   SMTP_PORT=587
 *   SMTP_USERNAME=oteuemail@gmail.com
 *   SMTP_PASSWORD=<app password gerada em myaccount.google.com/apppasswords>
 *   SMTP_FROM_NAME=Sistema de Planeamento de Horários   (opcional)
 *
 * Se SMTP_USERNAME ou SMTP_PASSWORD não estiverem definidas, o serviço fica
 * em modo "simulação": em vez de enviar, regista o email no log. Isto permite
 * correr/testar a aplicação sem credenciais reais.
 */
object EmailConfig {
    val host: String = System.getenv("SMTP_HOST") ?: "smtp.gmail.com"
    val port: String = System.getenv("SMTP_PORT") ?: "587"
    val username: String? = System.getenv("SMTP_USERNAME")
    val password: String? = System.getenv("SMTP_PASSWORD")
    val fromName: String = System.getenv("SMTP_FROM_NAME") ?: "Sistema de Planeamento de Horários"
    val enabled: Boolean = !username.isNullOrBlank() && !password.isNullOrBlank()
}

object EmailService {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    // Pool pequeno dedicado ao envio de email, para não bloquear a thread do pedido HTTP
    // nem depender de coroutines (o resto do backend é síncrono/bloqueante).
    private val executor = Executors.newFixedThreadPool(2)

    private fun buildSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", EmailConfig.host)
            put("mail.smtp.port", EmailConfig.port)
        }
        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication =
                PasswordAuthentication(EmailConfig.username, EmailConfig.password)
        })
    }

    private fun sendNow(to: String, subject: String, body: String) {
        if (!EmailConfig.enabled) {
            logger.warn("[EMAIL SIMULADO - SMTP não configurado] Para: $to | Assunto: $subject\n$body")
            return
        }
        try {
            val message = MimeMessage(buildSession())
            message.setFrom(InternetAddress(EmailConfig.username, EmailConfig.fromName))
            message.setRecipient(Message.RecipientType.TO, InternetAddress(to))
            message.subject = subject
            message.setText(body, "UTF-8")
            Transport.send(message)
            logger.info("Email enviado para $to (assunto: \"$subject\")")
        } catch (e: Exception) {
            logger.error("Falha ao enviar email para $to (assunto: \"$subject\")", e)
        }
    }

    /** Envia de forma assíncrona: nunca bloqueia nem lança exceção para quem chama. */
    fun sendAsync(to: String, subject: String, body: String) {
        executor.submit {
            try {
                sendNow(to, subject, body)
            } catch (e: Exception) {
                logger.error("Erro inesperado ao enviar email para $to", e)
            }
        }
    }

    fun notifyLessonCancelled(
        studentEmail: String,
        studentName: String,
        teacherName: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ) {
        val subject = "Aula cancelada - $date"
        val body = """
            Olá $studentName,

            A sua aula com $teacherName agendada para $date, das $startTime às $endTime, foi cancelada.

            Se tiver alguma dúvida, contacte o seu professor.

            Cumprimentos,
            ${EmailConfig.fromName}
        """.trimIndent()
        sendAsync(studentEmail, subject, body)
    }

    fun notifyLessonRescheduled(
        studentEmail: String,
        studentName: String,
        teacherName: String,
        oldDate: LocalDate,
        oldStart: LocalTime,
        oldEnd: LocalTime,
        newDate: LocalDate,
        newStart: LocalTime,
        newEnd: LocalTime
    ) {
        val subject = "Aula remarcada"
        val body = """
            Olá $studentName,

            A sua aula com $teacherName foi remarcada.

            De:   $oldDate, das $oldStart às $oldEnd
            Para: $newDate, das $newStart às $newEnd

            Cumprimentos,
            ${EmailConfig.fromName}
        """.trimIndent()
        sendAsync(studentEmail, subject, body)
    }

    fun notifySeriesCancelled(
        studentEmail: String,
        studentName: String,
        teacherName: String,
        affectedCount: Int
    ) {
        val subject = "Série de aulas cancelada"
        val body = """
            Olá $studentName,

            $teacherName cancelou a série de aulas recorrentes. $affectedCount aula(s) futura(s) foram canceladas.

            Cumprimentos,
            ${EmailConfig.fromName}
        """.trimIndent()
        sendAsync(studentEmail, subject, body)
    }

    /** Mensagem livre escrita pelo professor (aviso manual). */
    fun notifyCustom(
        studentEmail: String,
        studentName: String,
        teacherName: String,
        subject: String,
        message: String
    ) {
        val body = """
            Olá $studentName,

            $message

            — $teacherName

            (Mensagem enviada através do ${EmailConfig.fromName})
        """.trimIndent()
        sendAsync(studentEmail, subject, body)
    }
}
