package de.adorsys.oauth2.pkce.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.apis.CatchExceptionHamcrestMatchers.hasMessage;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public class RefererServiceTest {

    private RefererService refererService;

    @Before
    public void setup() throws Exception {
        refererService = new RefererService();
    }

    @Test
    public void shouldBuildRedirectUri() throws Exception {
        String redirectUri = refererService.buildRedirectUri("http://my_custom_referer", "/my_custom_path");
        assertThat(redirectUri, is(equalTo("http://my_custom_referer/my_custom_path")));
    }

    @Test
    public void shouldBuildRedirectUriWithoutStartingSlash() throws Exception {
        String redirectUri = refererService.buildRedirectUri("http://my_custom_referer", "my_custom_path");
        assertThat(redirectUri, is(equalTo("http://my_custom_referer/my_custom_path")));
    }

    @Test
    public void shouldBuildRedirectUriByIgnoringEndingSlash() throws Exception {
        String redirectUri = refererService.buildRedirectUri("http://my_custom_referer/", "/my_custom_path");
        assertThat(redirectUri, is(equalTo("http://my_custom_referer/my_custom_path")));
    }

    @Test
    public void shouldBuildRedirectUriByIgnoringAnyRefererPath() throws Exception {
        String redirectUri = refererService.buildRedirectUri("http://my_custom_referer/ignored_path", "/my_custom_path");
        assertThat(redirectUri, is(equalTo("http://my_custom_referer/my_custom_path")));
    }

    @Test
    public void shouldThrowErrorWhenTargetPathIsContainingQueryParams() throws Exception {
        catchException(refererService)
                .buildRedirectUri(
                        "http://my_custom_referer/ignored_path",
                        "/my_custom_path?query_param=value"
                );

        Assert.assertThat(caughtException(),
                allOf(
                        instanceOf(RefererService.BadTargetPathException.class),
                        hasMessage("Illegal target_path")
                )
        );
    }

    @Test
    public void shouldThrowErrorWhenTargetPathIsAbsoluteUrl() throws Exception {
        catchException(refererService)
                .buildRedirectUri(
                        "http://my_custom_referer/ignored_path",
                        "http://my-custom-target-path-as-absolute-url"
                );

        Assert.assertThat(caughtException(),
                allOf(
                        instanceOf(RefererService.BadTargetPathException.class),
                        hasMessage("Illegal target_path")
                )
        );
    }

    @Test
    public void shouldThrowErrorWhenTargetPathIsIllegalUrl() throws Exception {
        catchException(refererService)
                .buildRedirectUri(
                        "http://my_custom_referer/ignored_path",
                        "http://illegal_uri"
                );

        Assert.assertThat(caughtException(),
                allOf(
                        instanceOf(RefererService.BadTargetPathException.class),
                        hasMessage("Illegal target_path")
                )
        );
    }

}