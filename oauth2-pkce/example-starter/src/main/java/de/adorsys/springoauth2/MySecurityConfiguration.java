package de.adorsys.springoauth2;

import de.adorsys.oauth2.pkce.EnableOauth2PkceServer;
import de.adorsys.oauth2.pkce.filter.TokenProcessingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableOauth2PkceServer
@Order(6)
public class MySecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    TokenProcessingFilter tokenProcessingFilter;

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
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
            .addFilterBefore(tokenProcessingFilter, UsernamePasswordAuthenticationFilter.class)
        ;
    }
}
