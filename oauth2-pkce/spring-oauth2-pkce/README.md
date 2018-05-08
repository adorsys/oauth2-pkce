# spring-oauth2-pkce

## Development

### Dependencies

You can easily use this library in your spring boot app via maven dependency:

```
    <dependency>
        <artifactId>spring-oauth2-pkce</artifactId>
        <groupId>de.adorsys.oauth2-pkce</groupId>
        <version>${version}</version>
    </dependency>
```

### Annotations

You can easily use OAuth2-PKCE by adding the following annotation to your spring `@Configuration` class:

| Annotation | Description |
|------------|-------------|
| `@EnableOauth2PkceServer` | Enables the OAuth2-PKCE server |

### Security config

Register the TokenProcessingFilter to process the received bearer token from the client:

For example:
```
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
```

### Application config

You have to adjust the pkce settings in your application.yml:

```
pkce:
  authorization-header: <Authorization-Header needed for the IDP, (for example: "Basic dXNlcjpwYXNzd29yZA==")>
  client-id: <Your client-id>
  client-secret: <Your client-secret>
  access-token-uri: <The IDP's token endpoint, (like: https://my.idp/token)>
  user-authorization-uri: <The IDP's user-authorization endpoint, (like: https://my.idp/authorize)>
  user-info-uri: <The IDP's user-info endpoint, (like: https://my.idp/userinfo)>
  scope: <The oauth2 scope, (for example: openid+profile)>
  redirect-uri: <The oauth2 redirect url, (like: http://your.app/token)>
  code-challenge-method: <The PKCE challenge method, (default: S256)>
  response-type: <The PKCE response type, (default: code+id_token)>
  cookie-name: <The cookie name: (default: bearer_token)>
  secure-cookie: <Defines if the cooke is used only for https, (default: true)>
```
