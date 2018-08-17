package eu.europa.ec.sante.ehdsi.openncp.gateway.web;

import eu.europa.ec.sante.ehdsi.openncp.gateway.domain.PasswordModel;
import eu.europa.ec.sante.ehdsi.openncp.gateway.domain.UserPrincipal;
import eu.europa.ec.sante.ehdsi.openncp.gateway.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(MainController.class);
    private UserService userService;

    @Autowired
    public MainController(UserService userService) {
        this.userService = userService;
    }

    // Login form
    @RequestMapping("/login.html")
    public String login() {

        return "login";
    }

    // Login form with error
    @RequestMapping("/login-error.html")
    public String loginError(Model model) {

        model.addAttribute("loginError", true);
        return "login";
    }

    @GetMapping(value = "/logout")
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login.html?logout";
    }

    @GetMapping(value = "/user/updatepassword")
    public String updatePassword(Model model) {

        logger.info("Initializing User update password");
        model.addAttribute("wrapper", new PasswordModel());
        return "user/updatepassword";
    }

    @PostMapping(value = "/user/updatepassword")
    public String savePassword(@ModelAttribute(value = "wrapper") PasswordModel passwordModel) {

        UserPrincipal user = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        userService.updateUserPassword(user.getUser(), passwordModel);
        return "index";
    }
}
