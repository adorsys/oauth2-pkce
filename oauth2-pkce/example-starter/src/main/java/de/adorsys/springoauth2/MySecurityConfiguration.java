package de.adorsys.springoauth2;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

//@Configuration
//@EnableWebSecurity
//@EnableOauth2PkceServer
//@Order(6)
public class MySecurityConfiguration extends WebSecurityConfigurerAdapter {

//    @Autowired
//    TokenProcessingFilter tokenProcessingFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .authorizeRequests()
                .antMatchers("/oauth/pkce").permitAll()
                .anyRequest().authenticated()
                .and()
            .exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/"))
                .and()
            .logout()
                .logoutSuccessUrl("/").permitAll()
                .and()
            .csrf()
                .disable();
//            .addFilterBefore(tokenProcessingFilter, UsernamePasswordAuthenticationFilter.class)
        ;
    }
}
