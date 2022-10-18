package eu.europa.ec.sante.ehdsi.openncp.gateway.config;

import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:smpeditor.properties")
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST", "PUT", "DELETE");
    }

    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected TomcatWebServer getTomcatWebServer(org.apache.catalina.startup.Tomcat tomcat) {
                tomcat.enableNaming();
                return super.getTomcatWebServer(tomcat);
            }

            // Parameter for Datasource used in Embedded Tomcat
            @Override
            protected void postProcessContext(Context context) {
                ContextResource defaultResource = new ContextResource();
                defaultResource.setName("jdbc/ConfMgr");
                defaultResource.setType(DataSource.class.getName());
                defaultResource.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
                defaultResource.setProperty("factory", "com.zaxxer.hikari.HikariJNDIFactory");
                defaultResource.setProperty("jdbcUrl", "jdbc:mysql://localhost:3306/ehealth_properties");
                defaultResource.setProperty("username", "myUsername");
                defaultResource.setProperty("password", "myPassword");
                context.getNamingResources().addResource(defaultResource);

                ContextResource atnaResource = new ContextResource();
                atnaResource.setName("jdbc/OPEN_ATNA");
                atnaResource.setType(DataSource.class.getName());
                atnaResource.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
                atnaResource.setProperty("factory", "com.zaxxer.hikari.HikariJNDIFactory");
                atnaResource.setProperty("jdbcUrl", "jdbc:mysql://localhost:3306/ehealth_atna");
                atnaResource.setProperty("username", "myUsername");
                atnaResource.setProperty("password", "myPassword");
                context.getNamingResources().addResource(atnaResource);

                ContextResource eadcResource = new ContextResource();
                eadcResource.setName("jdbc/EADC");
                eadcResource.setType(DataSource.class.getName());
                eadcResource.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
                eadcResource.setProperty("factory", "com.zaxxer.hikari.HikariJNDIFactory");
                eadcResource.setProperty("jdbcUrl", "jdbc:mysql://localhost:3306/ehealth_eadc");
                eadcResource.setProperty("username", "myUsername");
                eadcResource.setProperty("password", "myPassword");
                context.getNamingResources().addResource(eadcResource);
            }
        };
    }
}
