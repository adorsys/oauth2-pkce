package de.adorsys.oauth2.pkce.endpoint;

import de.adorsys.oauth2.pkce.config.TestPkceConfiguration;
import de.adorsys.oauth2.pkce.model.CodeChallenge;
import de.adorsys.oauth2.pkce.model.CodeVerifier;
import de.adorsys.oauth2.pkce.model.Nonce;
import de.adorsys.oauth2.pkce.model.State;
import de.adorsys.oauth2.pkce.service.UserAgentStateService;
import de.adorsys.oauth2.pkce.util.Oauth2PkceFactory;
import de.adorsys.oauth2.pkce.util.TokenConstants;
import de.adorsys.utils.UriCookieUtils;
import de.adorsys.utils.UriQueryUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
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
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@ContextConfiguration(
        classes = {TestPkceConfiguration.class},
        initializers = {ConfigFileApplicationContextInitializer.class}
)
@ActiveProfiles("IT")
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestPkceConfiguration.class)
public class PkceLoginRestControllerIT {

    private static final String NONCE = "my_custom_nonce";
    private static final String STATE = "my_custom_state";
    private static final String CODE_VERIFIER = "my_custom_code_verifier";
    private static final String CODE_CHALLENGE = "my_custom_code_challenge";

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UserAgentStateService userAgentStateService;

    @Autowired
    Environment environment;

    @MockBean
    Oauth2PkceFactory oauth2PkceFactory;

    private String localServerAddress;
    private String localServerTokenEndpointAddress;

    @Before
    public void setup() throws Exception {
        given(oauth2PkceFactory.generateNonce()).willReturn(new Nonce(NONCE));
        given(oauth2PkceFactory.generateState()).willReturn(new State(STATE));

        CodeVerifier codeVerifier = new CodeVerifier(CODE_VERIFIER);
        given(oauth2PkceFactory.generateCodeVerifier()).willReturn(codeVerifier);
        given(oauth2PkceFactory.createCodeChallenge(codeVerifier)).willReturn(new CodeChallenge(CODE_CHALLENGE));

        localServerAddress = "http://localhost:" + environment.getProperty("local.server.port");
        localServerTokenEndpointAddress = localServerAddress + "/oauth2/token";
    }

    @Test
    public void shouldRedirectToIdpLoginPage() {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/login?redirect_uri=my_login_redirect_uri", HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));
        HttpHeaders responseHeaders = response.getHeaders();
        URI redirectLocation = responseHeaders.getLocation();

        assertThat(redirectLocation.getScheme(), is(equalTo("http")));
        assertThat(redirectLocation.getHost(), is(equalTo("localhost")));
        assertThat(redirectLocation.getPath(), is(equalTo("/auth/realms/moped/protocol/openid-connect/auth")));

        List<HttpCookie> responseCookies = UriCookieUtils.parseCookiesToList(responseHeaders);
        assertThat(responseCookies.size(), is(equalTo(1)));

        HttpCookie codeVerifierCookie = responseCookies.get(0);
        assertThat(codeVerifierCookie.getName(), is(equalTo(TokenConstants.CODE_VERIFIER_COOKIE_NAME)));
        assertThat(codeVerifierCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(codeVerifierCookie.getMaxAge(), is(equalTo(3600L)));
        assertThat(codeVerifierCookie.getValue(), is(equalTo(CODE_VERIFIER)));

        Map<String, String> queryParams = UriQueryUtils.getQueryMap(redirectLocation.getQuery());
        assertThat(queryParams.size(), is(equalTo(9)));
        assertThat(queryParams.get("client_id"), is(equalTo("moped-client")));
        assertThat(queryParams.get("scope"), is(equalTo("openid")));
        assertThat(queryParams.get("code_challenge_method"), is(equalTo("S256")));
        assertThat(queryParams.get("response_type"), is(equalTo("code")));
        assertThat(queryParams.get("response_mode"), is(equalTo("query")));
        assertThat(queryParams.get("redirect_uri"), is(equalTo("my_login_redirect_uri")));
        assertThat(queryParams.get("code_challenge"), is(equalTo(CODE_CHALLENGE)));
        assertThat(queryParams.get("nonce"), is(equalTo(NONCE)));
        assertThat(queryParams.get("state"), is(equalTo(STATE)));
    }

    @Test
    public void shouldRedirectToIdpLoginPageWithReferer() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Referer", "http://my_custom_referer/my_custom_referer_path");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/login", HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));
        HttpHeaders responseHeaders = response.getHeaders();
        URI redirectLocation = responseHeaders.getLocation();

        assertThat(redirectLocation.getScheme(), is(equalTo("http")));
        assertThat(redirectLocation.getHost(), is(equalTo("localhost")));
        assertThat(redirectLocation.getPath(), is(equalTo("/auth/realms/moped/protocol/openid-connect/auth")));

        MultiValueMap<String, HttpCookie> responseCookies = UriCookieUtils.parseCookiesAsMap(responseHeaders);
        assertThat(responseCookies.size(), is(equalTo(2)));

        List<HttpCookie> codeVerifierCookies = responseCookies.get(TokenConstants.CODE_VERIFIER_COOKIE_NAME);
        assertThat(codeVerifierCookies, hasSize(1));

        HttpCookie codeVerifierCookie = codeVerifierCookies.get(0);
        assertThat(codeVerifierCookie.getName(), is(equalTo(TokenConstants.CODE_VERIFIER_COOKIE_NAME)));
        assertThat(codeVerifierCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(codeVerifierCookie.getMaxAge(), is(equalTo(3600L)));
        assertThat(codeVerifierCookie.getValue(), is(equalTo(CODE_VERIFIER)));

        List<HttpCookie> userAgentStateCookies = responseCookies.get(TokenConstants.USER_AGENT_STATE_COOKIE_NAME);
        assertThat(userAgentStateCookies, hasSize(1));

        HttpCookie userAgentStateCookie = userAgentStateCookies.get(0);
        assertThat(userAgentStateCookie.getName(), is(equalTo(TokenConstants.USER_AGENT_STATE_COOKIE_NAME)));
        assertThat(userAgentStateCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(userAgentStateCookie.getMaxAge(), is(equalTo(3600L)));

        String userAgentStateValue = userAgentStateCookie.getValue();
        assertThat(userAgentStateValue, is(not(isEmptyString())));

        UserAgentStateService.UserAgentState userAgentState = userAgentStateService.readUserAgentState(userAgentStateValue);
        assertThat(userAgentState.getRedirectUri(), is(equalTo(localServerTokenEndpointAddress)));
        assertThat(userAgentState.getUserAgentPage(), is(equalTo("http://my_custom_referer/my_custom_referer_path")));

        Map<String, String> queryParams = UriQueryUtils.getQueryMap(redirectLocation.getQuery());
        assertThat(queryParams.size(), is(equalTo(9)));
        assertThat(queryParams.get("client_id"), is(equalTo("moped-client")));
        assertThat(queryParams.get("scope"), is(equalTo("openid")));
        assertThat(queryParams.get("code_challenge_method"), is(equalTo("S256")));
        assertThat(queryParams.get("response_type"), is(equalTo("code")));
        assertThat(queryParams.get("response_mode"), is(equalTo("query")));
        assertThat(queryParams.get("redirect_uri"), is(equalTo(localServerTokenEndpointAddress)));
        assertThat(queryParams.get("code_challenge"), is(equalTo(CODE_CHALLENGE)));
        assertThat(queryParams.get("nonce"), is(equalTo(NONCE)));
        assertThat(queryParams.get("state"), is(equalTo(STATE)));
    }

    @Test
    public void shouldRedirectToIdpLoginPageWithTargetPath() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Referer", "http://my_custom_referer/my_custom_referer_path");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/login?target_path=/my_custom_target_path", HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));
        HttpHeaders responseHeaders = response.getHeaders();
        URI redirectLocation = responseHeaders.getLocation();

        assertThat(redirectLocation.getScheme(), is(equalTo("http")));
        assertThat(redirectLocation.getHost(), is(equalTo("localhost")));
        assertThat(redirectLocation.getPath(), is(equalTo("/auth/realms/moped/protocol/openid-connect/auth")));

        MultiValueMap<String, HttpCookie> responseCookies = UriCookieUtils.parseCookiesAsMap(responseHeaders);
        assertThat(responseCookies.size(), is(equalTo(2)));

        List<HttpCookie> codeVerifierCookies = responseCookies.get(TokenConstants.CODE_VERIFIER_COOKIE_NAME);
        assertThat(codeVerifierCookies, hasSize(1));

        HttpCookie codeVerifierCookie = codeVerifierCookies.get(0);
        assertThat(codeVerifierCookie.getName(), is(equalTo(TokenConstants.CODE_VERIFIER_COOKIE_NAME)));
        assertThat(codeVerifierCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(codeVerifierCookie.getMaxAge(), is(equalTo(3600L)));
        assertThat(codeVerifierCookie.getValue(), is(equalTo(CODE_VERIFIER)));

        List<HttpCookie> userAgentStateCookies = responseCookies.get(TokenConstants.USER_AGENT_STATE_COOKIE_NAME);
        assertThat(userAgentStateCookies, hasSize(1));

        HttpCookie userAgentStateCookie = userAgentStateCookies.get(0);
        assertThat(userAgentStateCookie.getName(), is(equalTo(TokenConstants.USER_AGENT_STATE_COOKIE_NAME)));
        assertThat(userAgentStateCookie.getPath(), is(equalTo("/oauth2/token")));
        assertThat(userAgentStateCookie.getMaxAge(), is(equalTo(3600L)));

        String userAgentStateValue = userAgentStateCookie.getValue();
        assertThat(userAgentStateValue, is(not(isEmptyString())));

        UserAgentStateService.UserAgentState userAgentState = userAgentStateService.readUserAgentState(userAgentStateValue);
        assertThat(userAgentState.getRedirectUri(), is(equalTo(localServerTokenEndpointAddress)));
        assertThat(userAgentState.getUserAgentPage(), is(equalTo("http://my_custom_referer/my_custom_target_path")));

        Map<String, String> queryParams = UriQueryUtils.getQueryMap(redirectLocation.getQuery());
        assertThat(queryParams.size(), is(equalTo(9)));
        assertThat(queryParams.get("client_id"), is(equalTo("moped-client")));
        assertThat(queryParams.get("scope"), is(equalTo("openid")));
        assertThat(queryParams.get("code_challenge_method"), is(equalTo("S256")));
        assertThat(queryParams.get("response_type"), is(equalTo("code")));
        assertThat(queryParams.get("response_mode"), is(equalTo("query")));
        assertThat(queryParams.get("redirect_uri"), is(equalTo(localServerTokenEndpointAddress)));
        assertThat(queryParams.get("code_challenge"), is(equalTo(CODE_CHALLENGE)));
        assertThat(queryParams.get("nonce"), is(equalTo(NONCE)));
        assertThat(queryParams.get("state"), is(equalTo(STATE)));
    }

    @Test
    public void shouldRespondWith400WhenTargetPathIsInvalid() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Referer", "http://my_custom_referer/my_custom_referer_path");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/login?target_path=/my_custom_target_path?my_query_param=myvalue", HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }
}
