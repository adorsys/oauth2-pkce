package de.adorsys.oauth2.pkce.config;

import de.adorsys.oauth2.pkce.EnableOauth2PkceServer;
import de.adorsys.oauth2.pkce.PkceProperties;
import de.adorsys.oauth2.pkce.filter.CookiesAuthenticationFilter;
import de.adorsys.oauth2.pkce.filter.OpaqueTokenAuthenticationFilter;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import de.adorsys.sts.filter.JWTAuthenticationFilter;
import de.adorsys.sts.token.authentication.TokenAuthenticationService;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableOauth2PkceServer
public class TestSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private TokenAuthenticationService tokenAuthenticationService;
    private CookiesAuthenticationFilter cookiesAuthenticationFilter;
    private OpaqueTokenAuthenticationFilter opaqueTokenAuthenticationFilter;
    private PkceProperties pkceProperties;

    public TestSecurityConfiguration(
            TokenAuthenticationService tokenAuthenticationService,
            CookiesAuthenticationFilter cookiesAuthenticationFilter,
            PkceTokenRequestService pkceTokenRequestService,
            PkceProperties pkceProperties
    ) {
        super();
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.cookiesAuthenticationFilter = cookiesAuthenticationFilter;
        this.opaqueTokenAuthenticationFilter = new OpaqueTokenAuthenticationFilter(pkceTokenRequestService);
        this.pkceProperties = pkceProperties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.csrf().disable()
            .authorizeRequests()
                .antMatchers(
                        pkceProperties.getAuthEndpoint(),
                        pkceProperties.getTokenEndpoint()
                ).permitAll()
                .anyRequest().authenticated()
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .logout()
                .logoutSuccessUrl("/").permitAll()
                .and()
            .csrf()
                .disable();

        http.addFilterBefore(new JWTAuthenticationFilter(tokenAuthenticationService), BasicAuthenticationFilter.class)
            .addFilterBefore(opaqueTokenAuthenticationFilter, JWTAuthenticationFilter.class)
            .addFilterBefore(cookiesAuthenticationFilter, OpaqueTokenAuthenticationFilter.class);
    }
}
