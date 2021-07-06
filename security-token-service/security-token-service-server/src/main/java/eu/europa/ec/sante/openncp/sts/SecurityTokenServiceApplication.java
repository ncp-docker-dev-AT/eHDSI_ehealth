package eu.europa.ec.sante.openncp.sts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class SecurityTokenServiceApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SecurityTokenServiceApplication.class, args);
    }
}
