package de.adorsys.oauth2.pkce.endpoint;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import de.adorsys.oauth2.pkce.config.TestPkceConfiguration;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import de.adorsys.oauth2.pkce.service.UserAgentStateService;
import de.adorsys.utils.UriCookieUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(
        classes = {TestPkceConfiguration.class},
        initializers = {ConfigFileApplicationContextInitializer.class}
)
@ActiveProfiles("IT")
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PkceTokenRestControllerIT {
    private static final String AGENT_STATE_VALUE = "{\"redirectUri\":\"http://my-custom-redirect-uri\",\"userAgentPage\":\"http://my-custom-user-agent-page\"}";
    private static final String AGENT_STATE_BASE64 = Base64.encode(AGENT_STATE_VALUE.getBytes());
    private static final String CODE = "my_custom_code";
    private static final String CODE_VERIFIER = "my_custom_code_verifier";
    private static final String ACCESS_TOKEN_WITH_NONCE = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICItcjZTeHhSN0tzSm1xeXVZMGV2TlhfRlpqQ25pNURkZHl4a1B5SmVEaEdFIn0.eyJqdGkiOiI5ZjZiNDhmZC05MDI5LTQ0YTgtOGJiMS1hNTA4NWYxZTc2MTIiLCJleHAiOjE1NTgzMzU0MjAsIm5iZiI6MCwiaWF0IjoxNTU4MzM1MTIwLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvbW9wZWQiLCJzdWIiOiJiZDk2NzQwMy02MjE3LTQ4N2YtYTUwYi1mNzhhN2E4Y2RhZjYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJtb3BlZC1jbGllbnQiLCJub25jZSI6IjRYTFJiNGg1VllCZGFDWldJS2xfWGhka3JDWSIsImF1dGhfdGltZSI6MTU1ODMzNTEyMCwic2Vzc2lvbl9zdGF0ZSI6ImFmY2JmMGFjLTU2OWUtNDk5Zi1iZWQ5LTkwMDVmMTJhMmI4OSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdLCJzY29wZSI6Im9wZW5pZCBlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoibXkgZmlyc3RuYW1lIG15IGxhc3RuYW1lIiwicHJlZmVycmVkX3VzZXJuYW1lIjoibXl1c2VyIiwiZ2l2ZW5fbmFtZSI6Im15IGZpcnN0bmFtZSIsImZhbWlseV9uYW1lIjoibXkgbGFzdG5hbWUiLCJlbWFpbCI6Im15QG1haWwuZGUifQ.O9poOo7qmPZg9ixSyICsQmIwxu0CTDWK0GLBO6Z2AFEjDXVLEBLRcD3K6VN-gIUCcEHE932epTbosj8ZXQyd8gvDB9yb9_qaJ-UHzPF7jN_g4jSJY9ftaJIbk1LAG26I105onifO_uI9OcbDkZN64mChrRH4sIgqvJVRLwvKHiyPEh8mwplx_yVtfZ-SqKIriPYOy4aD4f1yiQ0xpVFK9GPtsV5hjnh3im7uQwsqYYtr433uC-VgQfpNaCjs1zs9COe30tAzyNYL4PxrhxlLg9jt1KMO0hC38wNXbVS7UYYHNWvQqjHvGoBojs6uWLn7IUvyJpqSq2uVKjlvpFcOF25EGOzvPzvYQ5GHBfqU64Y6DYc3bO9k8N49szEUS5-aOL2AtpNdETfxJECnJO3xoKtecC5xc0A8f4pjdm1T1SBbnExvcoseM3PJmL-bqfCen1K8oK1mrtCVS3JOtD7LB1mJMpGL4mFXQd9_4J4ECjGXNrLFqcgo8S1ykgwzkqKG2WEq551RnXW5x-vDmVA6k5bnoyxD1pEFxLR8xBnBnhxMpoMCzGZ9imHzWL0k78ITK1aoBdohwrVWUQygIOgHm6CrmbkfJVP7dXnd-6Lcg2WPP3smGLOIbUrbdejRrf3BtB2OWKpYk9IakcNmxqT35e-78-RrHfZ9K7vPblZ8YTg";
    private static final String REFRESH_TOKEN = "my_custom_refresh_token";
    private static final String NONCE = "4XLRb4h5VYBdaCZWIKl_XhdkrCY";
    private static final long ACCESS_TOKEN_EXPIRE_IN = 1234567L;
    private static final long REFRESH_TOKEN_EXPIRE_IN = 563246323L;
    private static final String REDIRECT_URI = "http://my-custom-redirect-uri";

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UserAgentStateService userAgentStateService;

    @MockBean
    PkceTokenRequestService pkceTokenRequestService;

    @Mock
    PkceTokenRequestService.TokenResponse tokenResponse;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(tokenResponse.getAccess_token()).thenReturn(ACCESS_TOKEN_WITH_NONCE);
        when(tokenResponse.getExpires_in()).thenReturn(ACCESS_TOKEN_EXPIRE_IN);
        when(tokenResponse.getRefresh_token()).thenReturn(REFRESH_TOKEN);
        when(tokenResponse.anyRefreshTokenExpireIn()).thenReturn(REFRESH_TOKEN_EXPIRE_IN);

        given(pkceTokenRequestService.requestToken(CODE, CODE_VERIFIER, REDIRECT_URI)).willReturn(tokenResponse);
    }

    @Test
    public void shouldRespondWith302WhenRequestTokenWithCode() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "code_verifier=" + CODE_VERIFIER);
        headers.add("Cookie", "user_agent_state=" + AGENT_STATE_BASE64);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/token?code=" + CODE, HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));

        HttpHeaders responseHeaders = response.getHeaders();
        URI redirectLocation = responseHeaders.getLocation();
        assertThat(redirectLocation.toString(), is(equalTo("http://my-custom-user-agent-page")));

        MultiValueMap<String, HttpCookie> responseCookies = UriCookieUtils.parseCookiesAsMap(responseHeaders);
        assertThat(responseCookies.size(), is(equalTo(5)));

        List<HttpCookie> accessTokenCookies = responseCookies.get("access_token");
        assertThat(accessTokenCookies, hasSize(1));

        HttpCookie accessTokenCookie = accessTokenCookies.get(0);
        assertThat(accessTokenCookie.getValue(), is(equalTo(ACCESS_TOKEN_WITH_NONCE)));
        assertThat(accessTokenCookie.getPath(), is(equalTo("/")));
        assertThat(accessTokenCookie.getMaxAge(), is(equalTo(ACCESS_TOKEN_EXPIRE_IN)));

        List<HttpCookie> refreshTokenCookies = responseCookies.get("refresh_token");
        assertThat(refreshTokenCookies, hasSize(1));

        HttpCookie refreshTokenCookie = refreshTokenCookies.get(0);
        assertThat(refreshTokenCookie.getValue(), is(equalTo(REFRESH_TOKEN)));
        assertThat(refreshTokenCookie.getPath(), is(equalTo("/")));
        assertThat(refreshTokenCookie.getMaxAge(), is(equalTo(REFRESH_TOKEN_EXPIRE_IN)));

        List<HttpCookie> codeVerifierCookies = responseCookies.get("code_verifier");
        assertThat(codeVerifierCookies, hasSize(1));

        HttpCookie codeVerifierCookie = codeVerifierCookies.get(0);
        assertThat(codeVerifierCookie.getValue(), is(equalTo("")));
        assertThat(codeVerifierCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(codeVerifierCookie.getMaxAge(), is(equalTo(0L)));

        List<HttpCookie> userAgentStateCookies = responseCookies.get("user_agent_state");
        assertThat(userAgentStateCookies, hasSize(1));

        HttpCookie userAgentStateCookie = userAgentStateCookies.get(0);
        assertThat(userAgentStateCookie.getValue(), is(equalTo("")));
        assertThat(userAgentStateCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(userAgentStateCookie.getMaxAge(), is(equalTo(0L)));

        List<HttpCookie> nonceCookies = responseCookies.get("nonce");
        assertThat(nonceCookies, hasSize(1));

        HttpCookie nonceCookie = nonceCookies.get(0);
        assertThat(nonceCookie.getValue(), is(equalTo("")));
        assertThat(nonceCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(nonceCookie.getMaxAge(), is(equalTo(0L)));
    }

    @Test
    public void shouldRespondWith302WhenRequestTokenWithCodeAndNonce() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "code_verifier=" + CODE_VERIFIER);
        headers.add("Cookie", "user_agent_state=" + AGENT_STATE_BASE64);
        headers.add("Cookie", "nonce=" + NONCE);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/token?code=" + CODE, HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));

        HttpHeaders responseHeaders = response.getHeaders();
        URI redirectLocation = responseHeaders.getLocation();
        assertThat(redirectLocation.toString(), is(equalTo("http://my-custom-user-agent-page")));

        MultiValueMap<String, HttpCookie> responseCookies = UriCookieUtils.parseCookiesAsMap(responseHeaders);
        assertThat(responseCookies.size(), is(equalTo(5)));

        List<HttpCookie> accessTokenCookies = responseCookies.get("access_token");
        assertThat(accessTokenCookies, hasSize(1));

        HttpCookie accessTokenCookie = accessTokenCookies.get(0);
        assertThat(accessTokenCookie.getValue(), is(equalTo(ACCESS_TOKEN_WITH_NONCE)));
        assertThat(accessTokenCookie.getPath(), is(equalTo("/")));
        assertThat(accessTokenCookie.getMaxAge(), is(equalTo(ACCESS_TOKEN_EXPIRE_IN)));

        List<HttpCookie> refreshTokenCookies = responseCookies.get("refresh_token");
        assertThat(refreshTokenCookies, hasSize(1));

        HttpCookie refreshTokenCookie = refreshTokenCookies.get(0);
        assertThat(refreshTokenCookie.getValue(), is(equalTo(REFRESH_TOKEN)));
        assertThat(refreshTokenCookie.getPath(), is(equalTo("/")));
        assertThat(refreshTokenCookie.getMaxAge(), is(equalTo(REFRESH_TOKEN_EXPIRE_IN)));

        List<HttpCookie> codeVerifierCookies = responseCookies.get("code_verifier");
        assertThat(codeVerifierCookies, hasSize(1));

        HttpCookie codeVerifierCookie = codeVerifierCookies.get(0);
        assertThat(codeVerifierCookie.getValue(), is(equalTo("")));
        assertThat(codeVerifierCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(codeVerifierCookie.getMaxAge(), is(equalTo(0L)));

        List<HttpCookie> userAgentStateCookies = responseCookies.get("user_agent_state");
        assertThat(userAgentStateCookies, hasSize(1));

        HttpCookie userAgentStateCookie = userAgentStateCookies.get(0);
        assertThat(userAgentStateCookie.getValue(), is(equalTo("")));
        assertThat(userAgentStateCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(userAgentStateCookie.getMaxAge(), is(equalTo(0L)));

        List<HttpCookie> nonceCookies = responseCookies.get("nonce");
        assertThat(nonceCookies, hasSize(1));

        HttpCookie nonceCookie = nonceCookies.get(0);
        assertThat(nonceCookie.getValue(), is(equalTo("")));
        assertThat(nonceCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(nonceCookie.getMaxAge(), is(equalTo(0L)));
    }

    @Test
    public void shouldRespondWith400WhenRequestTokenWithCodeAndNonceButAccessTokenContainsOtherNonce() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "code_verifier=" + CODE_VERIFIER);
        headers.add("Cookie", "user_agent_state=" + AGENT_STATE_BASE64);
        headers.add("Cookie", "nonce=" + "other_nonce_than_in_token");
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/token?code=" + CODE, HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void shouldRespondWith302WhenRequestTokenWithCodeAndRedirectUri() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "code_verifier=" + CODE_VERIFIER);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/token?code=" + CODE + "&redirect_uri=" + REDIRECT_URI, HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));

        HttpHeaders responseHeaders = response.getHeaders();
        URI redirectLocation = responseHeaders.getLocation();
        assertThat(redirectLocation.toString(), is(equalTo(REDIRECT_URI)));

        MultiValueMap<String, HttpCookie> responseCookies = UriCookieUtils.parseCookiesAsMap(responseHeaders);
        assertThat(responseCookies.size(), is(equalTo(5)));

        List<HttpCookie> accessTokenCookies = responseCookies.get("access_token");
        assertThat(accessTokenCookies, hasSize(1));

        HttpCookie accessTokenCookie = accessTokenCookies.get(0);
        assertThat(accessTokenCookie.getValue(), is(equalTo(ACCESS_TOKEN_WITH_NONCE)));
        assertThat(accessTokenCookie.getPath(), is(equalTo("/")));
        assertThat(accessTokenCookie.getMaxAge(), is(equalTo(ACCESS_TOKEN_EXPIRE_IN)));

        List<HttpCookie> refreshTokenCookies = responseCookies.get("refresh_token");
        assertThat(refreshTokenCookies, hasSize(1));

        HttpCookie refreshTokenCookie = refreshTokenCookies.get(0);
        assertThat(refreshTokenCookie.getValue(), is(equalTo(REFRESH_TOKEN)));
        assertThat(refreshTokenCookie.getPath(), is(equalTo("/")));
        assertThat(refreshTokenCookie.getMaxAge(), is(equalTo(REFRESH_TOKEN_EXPIRE_IN)));

        List<HttpCookie> codeVerifierCookies = responseCookies.get("code_verifier");
        assertThat(codeVerifierCookies, hasSize(1));

        HttpCookie codeVerifierCookie = codeVerifierCookies.get(0);
        assertThat(codeVerifierCookie.getValue(), is(equalTo("")));
        assertThat(codeVerifierCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(codeVerifierCookie.getMaxAge(), is(equalTo(0L)));

        List<HttpCookie> userAgentStateCookies = responseCookies.get("user_agent_state");
        assertThat(userAgentStateCookies, hasSize(1));

        HttpCookie userAgentStateCookie = userAgentStateCookies.get(0);
        assertThat(userAgentStateCookie.getValue(), is(equalTo("")));
        assertThat(userAgentStateCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(userAgentStateCookie.getMaxAge(), is(equalTo(0L)));

        List<HttpCookie> nonceCookies = responseCookies.get("nonce");
        assertThat(nonceCookies, hasSize(1));

        HttpCookie nonceCookie = nonceCookies.get(0);
        assertThat(nonceCookie.getValue(), is(equalTo("")));
        assertThat(nonceCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(nonceCookie.getMaxAge(), is(equalTo(0L)));
    }

    @Test
    public void shouldRespondWith400WhenRequestTokenWithCodeOnly() {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/token?code=" + CODE, HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void shouldRespondWith302WhenRequestTokenWithCodeAndRedirectUriButWithoutCodeVerifier() {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/token?code=" + CODE + "&redirect_uri=" + REDIRECT_URI, HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void shouldRespondWith302WhenRequestTokenWithRedirectAndCodeVerifierAndUserAgentStateButWithoutCode() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "code_verifier=" + CODE_VERIFIER);
        headers.add("Cookie", "user_agent_state=" + AGENT_STATE_BASE64);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/token?redirect_uri=" + REDIRECT_URI, HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void shouldRespondWith400WhenRequestTokenWithCodeAndCodeVerifierButWithoutUserAgentState() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "code_verifier=" + CODE_VERIFIER);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/token?code=" + CODE, HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }
}
