package com.yogurtvpn.features.vpn.data

import com.yogurtvpn.features.auth.domain.AuthRepository
import com.yogurtvpn.features.vpn.domain.EmailService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import org.slf4j.LoggerFactory
import java.util.UUID

class EmailServiceImpl(
    private val authRepository: AuthRepository,
    private val smtpHost: String,
    private val smtpPort: Int,
    private val username: String,
    private val password: String,
    private val fromAddress: String
): EmailService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val mailer = MailerBuilder
        .withSMTPServer(smtpHost, smtpPort, username, password)
        .withTransportStrategy(TransportStrategy.SMTPS)
        .withSessionTimeout(10_000)
        .buildMailer()

    override suspend fun sendVlessLink(userId: UUID, vlessLink: String) {
        val user = authRepository.findById(userId)
        if (user == null) {
            logger.warn("User $userId does not exist")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val email = EmailBuilder.startingBlank()
                    .from("YogurtVPN support", fromAddress)
                    .to(user.email)
                    .withSubject("Ваш VPN доступ одобрен")
                    .withHTMLText(buildEmailBody(vlessLink))
                    .buildEmail()

                mailer.sendMail(email)
                logger.info("Email sent to ${user.email}")
            } catch (e: Exception) {
                logger.error("Failed to send email to ${user.email}: ${e.message}", e)
            }
        }

    }

    private fun buildEmailBody(vlessLink: String): String = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
            <h2 style="color: #333;"> Ваш VPN доступ одобрен!</h2>
            <p>Ваша заявка на доступ к YogurtVPN была одобрена администратором.</p>
            
            <h3>Ваша VLESS-ссылка:</h3>
            <div style="background: #f4f4f4; padding: 15px; border-radius: 8px; word-break: break-all;">
                <code style="font-size: 12px;">$vlessLink</code>
            </div>
            
            <h3>Как подключиться:</h3>
            <ol>
                <li>Откройте приложение YogurtVPN</li>
                <li>Вставьте ссылку в поле импорта конфигурации</li>
                <li>Нажмите "Подключиться"</li>
            </ol>
            
            <p style="color: #666; font-size: 12px;">
                Не передавайте эту ссылку третьим лицам — она привязана к вашему аккаунту.
            </p>
        </body>
        </html>
        """.trimIndent()
}