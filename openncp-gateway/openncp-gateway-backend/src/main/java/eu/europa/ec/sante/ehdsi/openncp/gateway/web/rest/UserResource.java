package eu.europa.ec.sante.ehdsi.openncp.gateway.web.rest;

import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model.User;
import eu.europa.ec.sante.ehdsi.openncp.gateway.service.ExceptionFactory;
import eu.europa.ec.sante.ehdsi.openncp.gateway.service.ExceptionType;
import eu.europa.ec.sante.ehdsi.openncp.gateway.service.UserService;
import eu.europa.ec.sante.ehdsi.openncp.gateway.web.rest.model.PasswordReset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/api")
public class UserResource {

    private final Logger logger = LoggerFactory.getLogger(UserResource.class);
    private final UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/users")
    public ResponseEntity<List<User>> findUsers(@RequestParam(required = false) String q, Pageable pageable) {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/user/register")
    public ResponseEntity registerUser(@Valid @RequestBody User newUser) {
        List<User> users = userService.findAll();
        long lastId = 0L;

        if (!userService.isValidPassword(newUser.getPassword())) {
            logger.error("Invalid password : Length should between 8 and 30 characters with at least one uppercase letter, " +
                    "one lowercase letter, one number and one special character" +
                    "and no white spaces");
            return ResponseEntity.badRequest().body("{ \"body\": \"Invalid password\", \"statusCode\": \"BAD_REQUEST\", \"statusCodeValue\": 400 }");
        }

        for (User user : users) {
            if (user.getId().compareTo(lastId) >= 0) {
                lastId = user.getId() + 1;
            }
            if (user.equals(newUser)) {
                logger.info("[Gateway] User Already exists! {}", newUser.getUsername());
                return ResponseEntity.badRequest().body("{ \"body\": \"User Already exists!\", \"statusCode\": \"NOT_ACCEPTED\", \"statusCodeValue\": 403 }");
            }
            if (user.getId().equals(newUser.getId())) {
                logger.info("[Gateway] ID Already exists! {}", newUser.getId());
                return ResponseEntity.badRequest().body("{ \"body\": \"ID Already exists!\", \"statusCode\": \"NOT_ACCEPTED\", \"statusCodeValue\": 403 }");
            }
            if (user.getUsername().equals(newUser.getUsername())) {
                logger.info("[Gateway] Username Already exists! {}", newUser.getUsername());
                return ResponseEntity.badRequest().body("{ \"body\": \"Username Already exists!\", \"statusCode\": \"NOT_ACCEPTED\", \"statusCodeValue\": 403 }");
            }
        }
        newUser.setId(lastId);
        userService.createUser(newUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/update")
    public ResponseEntity updateUser(@Valid @RequestBody User updateUser) {
        List<User> users = userService.findAll();

        for (User user : users) {
            if (user.getId().equals(updateUser.getId())) {
                userService.updateUser(updateUser);
                return ResponseEntity.ok(users);
            }
        }

        return ResponseEntity.badRequest().body("{ \"body\": \"ID not found!\", \"statusCode\": \"NOT_ACCEPTED\", \"statusCodeValue\": 403 }");
    }

    @DeleteMapping("/user/delete")
    public ResponseEntity deleteUser(@RequestParam() User user) {

        user.setRoles(null);
        userService.deleteUser(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/user/reset-password/init")
    public ResponseEntity<String> requestPasswordReset(@RequestBody PasswordResetEmail email) {
        String message = userService.requestPasswordReset(email.getEmail());
        return ResponseEntity.ok(message);
    }

    @PostMapping(path = "/user/reset-password/finish")
    public ResponseEntity<String> completePasswordReset(@Valid @RequestBody PasswordResetCommand command) {
        if (!userService.isValidPassword(command.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("New password does not meet complexity rules");
        }
        
        userService.completePasswordReset(command.getToken(), command.getPassword());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/user/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody PasswordReset passwordReset) {
        if (!userService.isValidPassword(passwordReset.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("New password does not meet complexity rules");
        }

        if(!userService.changePassword(passwordReset.getPassword(), passwordReset.getOldPassword())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Current password does not match");
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
