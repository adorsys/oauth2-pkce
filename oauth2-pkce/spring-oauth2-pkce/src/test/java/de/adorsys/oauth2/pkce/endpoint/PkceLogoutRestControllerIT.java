package de.adorsys.oauth2.pkce.endpoint;

import de.adorsys.oauth2.pkce.config.TestPkceConfiguration;
import de.adorsys.oauth2.pkce.model.UserInfo;
import de.adorsys.oauth2.pkce.service.PkceTokenRequestService;
import de.adorsys.sts.tokenauth.BearerToken;
import de.adorsys.sts.tokenauth.BearerTokenValidator;
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
public class PkceLogoutRestControllerIT {

    private static final String REFERER = "http://my_custom_referer/my_custom_referer_path";
    private static final String ACCESS_TOKEN = "my_custom_access_token";
    private static final String REDIRECT_URI = "http://my-custom-redirect-uri";
    private static final String LOGOUT_URL = "http://localhost:8080/auth/realms/moped/protocol/openid-connect/logout";

    @Autowired
    TestRestTemplate restTemplate;

    @MockBean
    BearerTokenValidator bearerTokenValidator;

    @Mock
    BearerToken bearerToken;

    @MockBean
    PkceTokenRequestService pkceTokenRequestService;

    @Mock
    UserInfo userInfo;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(bearerToken.isValid()).thenReturn(true);
        given(bearerTokenValidator.extract(ACCESS_TOKEN)).willReturn(bearerToken);

        given(pkceTokenRequestService.userInfo(ACCESS_TOKEN)).willReturn(userInfo);
    }

    @Test
    public void shouldRespondWith302WhenRequestLogoutWithReferer() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "access_token=" + ACCESS_TOKEN);
        headers.add("Cookie", "refresh_token=" + "my_custom_refresh_token");
        headers.add("Referer", REFERER);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/logout", HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));
        HttpHeaders responseHeaders = response.getHeaders();
        URI redirectLocation = responseHeaders.getLocation();

        assertThat(redirectLocation.toString(), is(equalTo(LOGOUT_URL + "?redirect_uri=" + REFERER)));

        MultiValueMap<String, HttpCookie> responseCookies = UriCookieUtils.parseCookiesAsMap(responseHeaders);
        assertThat(responseCookies.size(), is(equalTo(2)));

        List<HttpCookie> accessTokenCookies = responseCookies.get("access_token");
        assertThat(accessTokenCookies, hasSize(1));

        HttpCookie accessTokenCookie = accessTokenCookies.get(0);
        assertThat(accessTokenCookie.getValue(), is(equalTo("")));
        assertThat(accessTokenCookie.getPath(), is(equalTo("/")));
        assertThat(accessTokenCookie.getMaxAge(), is(equalTo(0L)));

        List<HttpCookie> refreshTokenCookies = responseCookies.get("refresh_token");
        assertThat(refreshTokenCookies, hasSize(1));

        HttpCookie refreshTokenCookie = refreshTokenCookies.get(0);
        assertThat(refreshTokenCookie.getValue(), is(equalTo("")));
        assertThat(refreshTokenCookie.getPath(), is(equalTo("/")));
        assertThat(refreshTokenCookie.getMaxAge(), is(equalTo(0L)));
    }

    @Test
    public void shouldRespondWith302WhenRequestLogoutWithRedirectUri() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "access_token=" + ACCESS_TOKEN);
        headers.add("Cookie", "refresh_token=" + "my_custom_refresh_token");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/logout?redirect_uri=" + REDIRECT_URI, HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));
        HttpHeaders responseHeaders = response.getHeaders();
        URI redirectLocation = responseHeaders.getLocation();

        assertThat(redirectLocation.toString(), is(equalTo(LOGOUT_URL + "?redirect_uri=" + REDIRECT_URI)));

        MultiValueMap<String, HttpCookie> responseCookies = UriCookieUtils.parseCookiesAsMap(responseHeaders);
        assertThat(responseCookies.size(), is(equalTo(2)));

        List<HttpCookie> accessTokenCookies = responseCookies.get("access_token");
        assertThat(accessTokenCookies, hasSize(1));

        HttpCookie accessTokenCookie = accessTokenCookies.get(0);
        assertThat(accessTokenCookie.getValue(), is(equalTo("")));
        assertThat(accessTokenCookie.getPath(), is(equalTo("/")));
        assertThat(accessTokenCookie.getMaxAge(), is(equalTo(0L)));

        List<HttpCookie> refreshTokenCookies = responseCookies.get("refresh_token");
        assertThat(refreshTokenCookies, hasSize(1));

        HttpCookie refreshTokenCookie = refreshTokenCookies.get(0);
        assertThat(refreshTokenCookie.getValue(), is(equalTo("")));
        assertThat(refreshTokenCookie.getPath(), is(equalTo("/")));
        assertThat(refreshTokenCookie.getMaxAge(), is(equalTo(0L)));
    }

    @Test
    public void shouldRespondWith400WhenRequestLogoutWithoutRefererAndRedirectUri() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "access_token=" + ACCESS_TOKEN);
        headers.add("Cookie", "refresh_token=" + "my_custom_refresh_token");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = restTemplate.exchange("/oauth2/logout", HttpMethod.GET, new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));
        HttpHeaders responseHeaders = response.getHeaders();
        URI redirectLocation = responseHeaders.getLocation();

        assertThat(redirectLocation.toString(), is(equalTo(LOGOUT_URL)));

        MultiValueMap<String, HttpCookie> responseCookies = UriCookieUtils.parseCookiesAsMap(responseHeaders);
        assertThat(responseCookies.size(), is(equalTo(2)));

        List<HttpCookie> accessTokenCookies = responseCookies.get("access_token");
        assertThat(accessTokenCookies, hasSize(1));

        HttpCookie accessTokenCookie = accessTokenCookies.get(0);
        assertThat(accessTokenCookie.getValue(), is(equalTo("")));
        assertThat(accessTokenCookie.getPath(), is(equalTo("/")));
        assertThat(accessTokenCookie.getMaxAge(), is(equalTo(0L)));

        List<HttpCookie> refreshTokenCookies = responseCookies.get("refresh_token");
        assertThat(refreshTokenCookies, hasSize(1));

        HttpCookie refreshTokenCookie = refreshTokenCookies.get(0);
        assertThat(refreshTokenCookie.getValue(), is(equalTo("")));
        assertThat(refreshTokenCookie.getPath(), is(equalTo("/")));
        assertThat(refreshTokenCookie.getMaxAge(), is(equalTo(0L)));
    }
}
