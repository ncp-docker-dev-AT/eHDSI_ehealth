package eu.europa.ec.sante.ehdsi.openncp.gateway.web.rest;

import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model.User;
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
        /*
        for (Role newUserRole : newUser.getRoles()) {
            List<Role> roles = roleService.getRoles();
            for (Role role : roles) {
                if(role.getId().equals(newUserRole.getId())) {
                    LOGGER.info("[Gateway] role ID already exists! {}", newUserRole.getId());
                    return ResponseEntity.badRequest().body("{ \"body\": \"role ID already exists!\", \"statusCode\": \"NOT_ACCEPTED\", \"statusCodeValue\": 403 }");
                }
                if(role.getName().equals(newUserRole.getName())) {
                    LOGGER.info("[Gateway] Role name already exists! {}", newUserRole.getName());
                    return ResponseEntity.badRequest().body("{ \"body\": \"Role name already exists!\", \"statusCode\": \"NOT_ACCEPTED\", \"statusCodeValue\": 403 }");
                }
            }
        }
        */
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
    public ResponseEntity<Void> completePasswordReset(@Valid @RequestBody PasswordResetCommand command) {
//        if (!command.getPassword().equals(command.getPassword_confirm())) {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
        userService.completePasswordReset(command.getToken(), command.getPassword());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/user/change-password")
    public ResponseEntity changePassword(@Valid @RequestBody PasswordReset passwordReset) {

//        if (!passwordReset.getPassword().equals(passwordReset.getPassword_confirm())
//                || (passwordReset.getOldPassword() == null && passwordReset.getToken() == null)
//                || (passwordReset.getOldPassword() != null && passwordReset.getToken() != null)) {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//
//        if(passwordReset.getOldPassword() != null){
//            userService.changePassword(passwordReset.getUserId(), passwordReset.getOldPassword(), passwordReset.getPassword());
//        }
//
//        if(passwordReset.getToken() != null){
//            userService.changePasswordWithToken(passwordReset.getToken(), passwordReset.getPassword());
//        }
        userService.changePassword(passwordReset.getPassword(), passwordReset.getOldPassword());
        //auditLogService.log(null, AuditLogAction.UPDATE_USER_PASSWORD);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
