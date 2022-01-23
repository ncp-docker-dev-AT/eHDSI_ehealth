package eu.europa.ec.sante.ehdsi.openncp.gateway.service;

import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManager;
import eu.europa.ec.sante.ehdsi.openncp.configmanager.ConfigurationManagerFactory;
import eu.europa.ec.sante.ehdsi.openncp.gateway.config.ApplicationProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Service
public class MailService implements MessageSourceAware {

    private final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final ApplicationProperties applicationProperties;

    private final JavaMailSender mailSender;

    private MessageSourceAccessor messages;

    public MailService(ApplicationProperties applicationProperties, JavaMailSender mailSender) {
        this.applicationProperties = applicationProperties;
        this.mailSender = mailSender;
    }

    @Override
    public void setMessageSource(@NonNull MessageSource messageSource) {
        messages = new MessageSourceAccessor(messageSource);
    }

    @Async
    public void sendMail(String to, String subject, String content, boolean multipart, boolean html) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, multipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(applicationProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, html);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            logger.error("MessagingException: '{}'", e.getMessage());
        }
    }

    @Async
    public String sendMailFromTemplate(User user, String titleKey) {
        ConfigurationManager configurationManager = ConfigurationManagerFactory.getConfigurationManager();
        boolean mail = configurationManager.getBooleanProperty("GTW_MAIL_ENABLED");
        String content = "Change your password <a href='" +
                applicationProperties.getPortal().getBaseUrl() +
                "/#/reset?key=" +
                user.getResetKey() +
                "'>here</a>";
        if (mail) {
            String subject = messages.getMessage(titleKey, "subject");
            sendMail(user.getEmail(), subject, content, false, true);
        }
        return content;
    }

    @Async
    public String sendPasswordResetMail(User user) {
        return sendMailFromTemplate(user, "Mail.PasswordReset.Title");
    }
}
