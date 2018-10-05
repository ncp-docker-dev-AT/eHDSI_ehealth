package se.sb.epsos.web.auth;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleUserDetailsService implements UserDetailsService {

    private static final String KEY_USER_PREFIX = "Users.";
    private final Logger logger = LoggerFactory.getLogger(SimpleUserDetailsService.class);
    private Resource usersXML;

    private XMLConfiguration config;

    @Override
    public UserDetails loadUserByUsername(String username) {

        logger.info("Checking userinfo for username '{}'", username);
        if (config == null) {
            try {
                init();
            } catch (ConfigurationException e) {
                logger.error("ConfigurationException: '{}'", e.getMessage(), e);
                throw new DataAccessException("Failed to load users from XML") {
                    private static final long serialVersionUID = -3848074168433511697L;
                };
            } catch (IOException e) {
                logger.error("IOException: '{}'", e.getMessage(), e);
                throw new DataAccessException("Failed to read XML file") {
                    private static final long serialVersionUID = 2763171318518245251L;
                };
            }
        }
        String passwd = config.getString(KEY_USER_PREFIX + username + "[@password]");
        if (passwd == null) {
            logger.info("Username '{}' was not found in catalogue", username);
            throw new UsernameNotFoundException("User " + username + " was not recognised");
        }
        logger.info("Username '{}' was found in catalogue, loading user details", username);
        AuthenticatedUser userDetails = new AuthenticatedUser(username, passwd);
        userDetails.setCommonName(config.getString(KEY_USER_PREFIX + username + ".commonName"));
        userDetails.setOrganizationId(config.getString(KEY_USER_PREFIX + username + ".organizationId"));
        userDetails.setOrganizationName(config.getString(KEY_USER_PREFIX + username + ".organizationName"));
        userDetails.setUserId(config.getString(KEY_USER_PREFIX + username + ".userId"));
        userDetails.setGivenName(config.getString(KEY_USER_PREFIX + username + ".givenName"));
        userDetails.setFamilyName(config.getString(KEY_USER_PREFIX + username + ".familyName"));
        userDetails.setTelecom(config.getString(KEY_USER_PREFIX + username + ".telecom"));
        userDetails.setStreet(config.getString(KEY_USER_PREFIX + username + ".street"));
        userDetails.setPostalCode(config.getString(KEY_USER_PREFIX + username + ".postalCode"));
        userDetails.setCity(config.getString(KEY_USER_PREFIX + username + ".city"));

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) (List) config.getList(KEY_USER_PREFIX + username + ".roles.role");
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        userDetails.setAuthorities(authorities);
        if (logger.isInfoEnabled()) {
            logger.info("Loaded AuthenticatedUser: '{}'", userDetails);
        }
        return userDetails;
    }

    private void init() throws ConfigurationException, IOException {

        if (usersXML == null) {
            throw new ConfigurationException("Users xml config file path not set");
        }
        logger.info("Initializing user catalogue from XML in '{}'", usersXML.getURL().getPath());
        config = new XMLConfiguration(usersXML.getURL());
    }

    public Resource getUsersXML() {
        return usersXML;
    }

    public void setUsersXML(Resource usersXML) {
        this.usersXML = usersXML;
    }
}
