package eu.europa.ec.sante.ehdsi.openncp.gateway.service;

import com.querydsl.core.BooleanBuilder;
import eu.europa.ec.sante.ehdsi.openncp.gateway.config.ApplicationProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.config.SmtpProperties;
import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model.User;
import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.repository.UserRepository;
import eu.europa.ec.sante.ehdsi.openncp.gateway.security.SecurityUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.passay.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.time.Instant;
import java.util.*;

@Service
@Transactional()
public class UserService {

    private final UserRepository userRepository;

    private final MailService mailService;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationProperties applicationProperties;

    private final SmtpProperties smtpProperties;

    public UserService(UserRepository userRepository, MailService mailService, PasswordEncoder passwordEncoder,
                       ApplicationProperties applicationProperties, SmtpProperties smtpProperties) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.applicationProperties = applicationProperties;
        this.smtpProperties = smtpProperties;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User createUser(User u) {
        setPassword(u, u.getPassword());
        User ret = userRepository.save(u);
        userRepository.flush();
        return ret;
    }

    public User updateUser(User u) {
        User ret = userRepository.save(u);
        userRepository.flush();
        return ret;
    }

    public Page<User> findUsers(String search, Pageable pageable) {
        return userRepository.findAllWithRoles(new BooleanBuilder(), pageable);
    }

    public boolean deleteUser(User user) {
        userRepository.delete(user);
        userRepository.flush();
        return true;
    }

    private void sendPasswordResetMail(User user) throws MessagingException {

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", smtpProperties.getSmtp().getAuth());
        properties.put("mail.smtp.starttls.enable", smtpProperties.getSmtp().getStartTls().getEnabled());
        properties.put("mail.smtp.host", smtpProperties.getHost());
        properties.put("mail.smtp.port", smtpProperties.getPort());

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpProperties.getUsername(), smtpProperties.getPassword());
            }
        });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(applicationProperties.getMail().getFrom(), false));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
        message.setSubject("MyHealth@EU - Gateway Admin reset password");
        message.setContent("Here is your key for resetting the password: " + user.getResetKey(), "text/html");
        message.setSentDate(new Date());

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent("MyHealth@EU - Gateway Admin reset password: " + user.getResetKey(), "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        message.setContent(multipart);
        Transport.send(message);
    }

    public String requestPasswordReset(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().isEnabled()) {
            user.get().setResetKey(RandomStringUtils.randomAlphanumeric(32));
            user.get().setResetDate(Instant.now());
            return mailService.sendPasswordResetMail(user.get());
        }
        return "User not found!!!";
    }

    public void completePasswordReset(String key, String password) {
        Optional<User> user = userRepository.findByResetKey(key);

        if (user.isPresent() && user.get().getResetDate().isAfter(Instant.now().minusSeconds(3600))) {
            setPassword(user.get(), password);
            user.get().setResetKey(null);
            user.get().setResetDate(null);
        }
    }

    public void abortPasswordReset(String key, String password) {
        Optional<User> user = userRepository.findByResetKey(key);

        if (user.isPresent() && user.get().getResetDate().isAfter(Instant.now().minusSeconds(3600))) {
            user.get().setResetKey(null);
            user.get().setResetDate(null);
        }
    }

    public void changePasswordWithToken(String token, String password) {
        Optional<User> user = userRepository.findByResetKey(token);

        if (user.isPresent() && user.get().getResetDate().isAfter(Instant.now().minusSeconds(86400))) {
            setPassword(user.get(), password);
            user.get().setResetKey(null);
            user.get().setResetDate(null);
        }
    }

    public boolean changePassword(String password, String oldPassword) {

        Optional<User> user = userRepository.findByUsername(SecurityUtils.getUsername());

        if (user.isPresent() && passwordEncoder.matches(oldPassword, user.get().getPassword())) {
            setPassword(user.get(), password);
            user.get().setResetKey(null);
            user.get().setResetDate(null);
            userRepository.saveAndFlush(user.get());
        } else {
            return false;
        }
        return true;
    }

    private void setPassword(User user, String password) {

//        if (!isValidPassword(password)) {
//            throw ExceptionFactory.create(ExceptionType.PWD_INVALID_FORMAT);
//        }
        user.setPassword(passwordEncoder.encode(password));
    }

    public boolean isValidPassword(String password) {

        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 30),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule()));

        RuleResult result = validator.validate(new PasswordData(password));
        return result.isValid();
    }
}
