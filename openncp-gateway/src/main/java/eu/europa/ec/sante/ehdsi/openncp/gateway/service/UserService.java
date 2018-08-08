package eu.europa.ec.sante.ehdsi.openncp.gateway.service;

import eu.europa.ec.sante.ehdsi.openncp.gateway.domain.PasswordModel;
import eu.europa.ec.sante.ehdsi.openncp.gateway.domain.User;
import eu.europa.ec.sante.ehdsi.openncp.gateway.domain.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void updateUserPassword(User user, PasswordModel passwordModel) {

        if (StringUtils.equals(passwordModel.getMatchPassword(), passwordModel.getNewPassword())) {

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String encryptedPassword = encoder.encode(passwordModel.getNewPassword());
            user.setPassword(encryptedPassword);
            userRepository.save(user);
        } else {
            logger.error("User credentials cannot be updated, New Password does not match");
        }
    }
}
