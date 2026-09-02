package ld.application.infra.email.core;

import jakarta.mail.MessagingException;
import ld.application.infra.email.config.EmailProperties;
import ld.application.infra.email.exception.EmailSendingException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SmtpEmailSender
        implements EmailSender {

    private final JavaMailSender mailSender;
    private final String from;
    private final boolean enabled;
    public SmtpEmailSender(
            JavaMailSender mailSender,
            EmailProperties properties
    ) {
        this.mailSender = mailSender;
        this.from = properties.from();
        this.enabled = properties.enabled();
    }

    @Override
    public void send(Email email) {
        if (!enabled) {
            return;
        }
        try {
            var message =
                    mailSender.createMimeMessage();

            var helper = new MimeMessageHelper(
                    message,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(from);
            helper.setTo(email.recipient());
            helper.setSubject(email.subject());
            helper.setText(email.content(), true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new EmailSendingException(e);
        }
    }
}
