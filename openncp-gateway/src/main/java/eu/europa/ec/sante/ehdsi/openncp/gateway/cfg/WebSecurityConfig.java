package eu.europa.ec.sante.ehdsi.openncp.gateway.cfg;

import eu.europa.ec.sante.ehdsi.openncp.gateway.domain.GatewayConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity
@ComponentScan("eu.europa.ec.sante.ehdsi.openncp.gateway.domain")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;

    @Autowired
    public WebSecurityConfig(@Qualifier("customUserDetailsService") UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {

        auth.authenticationProvider(authProvider());
    }

    @Override
    public void configure(WebSecurity web) {
        // @formatter:off
        web
                .ignoring()
                .antMatchers("/resources/**");
        // @formatter:on
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //.csrf().disable()
                .authorizeRequests()
                // Web
                .mvcMatchers("/login.html").permitAll()
                .mvcMatchers("/", "/index").hasAuthority(GatewayConstant.GTW_ROLE_SMP_ADMIN)
                .antMatchers("/monitoring/**").hasAuthority(GatewayConstant.GTW_ROLE_SMP_ADMIN)
                .antMatchers("/smpeditor/**").hasAuthority(GatewayConstant.GTW_ROLE_SMP_ADMIN)
                .antMatchers("/dynamicdiscovery/**").hasAuthority(GatewayConstant.GTW_ROLE_SMP_ADMIN)
                .anyRequest().authenticated().and().formLogin()
                .loginPage("/login.html")
                .and().logout().permitAll()
                .and().httpBasic();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
