package de.adorsys.oauth2.pkce.service;

import de.adorsys.oauth2.pkce.model.Nonce;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class NonceValidationTest {

    private static final String ACCESS_TOKEN_WITH_NONCE = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICItcjZTeHhSN0tzSm1xeXVZMGV2TlhfRlpqQ25pNURkZHl4a1B5SmVEaEdFIn0.eyJqdGkiOiI5ZjZiNDhmZC05MDI5LTQ0YTgtOGJiMS1hNTA4NWYxZTc2MTIiLCJleHAiOjE1NTgzMzU0MjAsIm5iZiI6MCwiaWF0IjoxNTU4MzM1MTIwLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvbW9wZWQiLCJzdWIiOiJiZDk2NzQwMy02MjE3LTQ4N2YtYTUwYi1mNzhhN2E4Y2RhZjYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJtb3BlZC1jbGllbnQiLCJub25jZSI6IjRYTFJiNGg1VllCZGFDWldJS2xfWGhka3JDWSIsImF1dGhfdGltZSI6MTU1ODMzNTEyMCwic2Vzc2lvbl9zdGF0ZSI6ImFmY2JmMGFjLTU2OWUtNDk5Zi1iZWQ5LTkwMDVmMTJhMmI4OSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdLCJzY29wZSI6Im9wZW5pZCBlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoibXkgZmlyc3RuYW1lIG15IGxhc3RuYW1lIiwicHJlZmVycmVkX3VzZXJuYW1lIjoibXl1c2VyIiwiZ2l2ZW5fbmFtZSI6Im15IGZpcnN0bmFtZSIsImZhbWlseV9uYW1lIjoibXkgbGFzdG5hbWUiLCJlbWFpbCI6Im15QG1haWwuZGUifQ.O9poOo7qmPZg9ixSyICsQmIwxu0CTDWK0GLBO6Z2AFEjDXVLEBLRcD3K6VN-gIUCcEHE932epTbosj8ZXQyd8gvDB9yb9_qaJ-UHzPF7jN_g4jSJY9ftaJIbk1LAG26I105onifO_uI9OcbDkZN64mChrRH4sIgqvJVRLwvKHiyPEh8mwplx_yVtfZ-SqKIriPYOy4aD4f1yiQ0xpVFK9GPtsV5hjnh3im7uQwsqYYtr433uC-VgQfpNaCjs1zs9COe30tAzyNYL4PxrhxlLg9jt1KMO0hC38wNXbVS7UYYHNWvQqjHvGoBojs6uWLn7IUvyJpqSq2uVKjlvpFcOF25EGOzvPzvYQ5GHBfqU64Y6DYc3bO9k8N49szEUS5-aOL2AtpNdETfxJECnJO3xoKtecC5xc0A8f4pjdm1T1SBbnExvcoseM3PJmL-bqfCen1K8oK1mrtCVS3JOtD7LB1mJMpGL4mFXQd9_4J4ECjGXNrLFqcgo8S1ykgwzkqKG2WEq551RnXW5x-vDmVA6k5bnoyxD1pEFxLR8xBnBnhxMpoMCzGZ9imHzWL0k78ITK1aoBdohwrVWUQygIOgHm6CrmbkfJVP7dXnd-6Lcg2WPP3smGLOIbUrbdejRrf3BtB2OWKpYk9IakcNmxqT35e-78-RrHfZ9K7vPblZ8YTg";
    private static final String ACCESS_TOKEN_WITHOUT_NONCE = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJvVjU2Uk9namthbTVzUmVqdjF6b1JVNmY1R3YtUGRTdjN2b1ZfRVY5MmxnIn0.eyJqdGkiOiI5NWY2MzQ4NC04MTk2LTQ2NzYtYjI4Ni1lYjY4YTFmOTZmYTAiLCJleHAiOjE1NTUwNDg5MzIsIm5iZiI6MCwiaWF0IjoxNTU1MDQ4NjMyLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjMyODU0L2F1dGgvcmVhbG1zL21vcGVkIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImJiNjNkN2Y2LWFhZjUtNDc5My1iNjA0LTY2NWZhMzU0YmU0MSIsInR5cCI6IkJlYXJlciIsImF6cCI6Im1vcGVkLWNsaWVudCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6ImZiZTU3ODNlLTE5NmUtNGM5Yi04OThhLTVkMmE2MDQ1MmM0NSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdLCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiTXkgVXNlciAxIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlcjEiLCJnaXZlbl9uYW1lIjoiTXkiLCJmYW1pbHlfbmFtZSI6IlVzZXIgMSIsImVtYWlsIjoibXkxQG1haWwuZGUifQ.VMIYfwGNDc3j2JAp_ZIXaITpwTnamYEMBX_FxVuS55_t3bbxx4WjR7N2zBwUlVd6HaxrHBPvbCyUzEhhjtP5BJcHaS1kN4A3zv215F_Za1gM-Im7wUQ9Ggg9bIPbWbHmjVBldk8oCGyeGIkGT5U12iJ376wFSX-IVHnfpAjgbRtfLKqYKS7zn0L0p2KZtjjdwz0CzG7r20qD2QfgDoA0CpOZCQzMe9WoIfo8L-g4099--XouFyMWRU8VyVsx_73ekNKPUmWvuNIxeF3PBk9KGs7ABUnv_6n8A-KqzYTyA4y0gU8E9mgIuWpDmQ2FROf1Gd-2it9k3tvr83k7N1dMvg";

    NonceValidation nonceValidation;


    @Before
    public void setup() throws Exception {
      nonceValidation = new NonceValidation();
    }

    @Test
    public void shouldReturnTrueIfTokenHasNonce() throws Exception {
        String expectedNonce = "4XLRb4h5VYBdaCZWIKl_XhdkrCY";

        boolean hasNonce = nonceValidation.hasNonce(ACCESS_TOKEN_WITH_NONCE, new Nonce(expectedNonce));
        assertThat(hasNonce, is(true));
    }

    @Test
    public void shouldReturnFalseIfTokenHasNotSameNonce() throws Exception {
        String expectedNonce = "another_nonce_value";

        boolean hasNonce = nonceValidation.hasNonce(ACCESS_TOKEN_WITH_NONCE, new Nonce(expectedNonce));
        assertThat(hasNonce, is(false));
    }

    @Test
    public void shouldReturnFalseIfAccessTokenHasNoNonce() throws Exception {
        String expectedNonce = "4XLRb4h5VYBdaCZWIKl_XhdkrCY";

        boolean hasNonce = nonceValidation.hasNonce(ACCESS_TOKEN_WITHOUT_NONCE, new Nonce(expectedNonce));
        assertThat(hasNonce, is(false));
    }

    @Test
    public void shouldReturnFalseIfNonceIsEmpty() throws Exception {
        String expectedNonce = "";

        boolean hasNonce = nonceValidation.hasNonce(ACCESS_TOKEN_WITH_NONCE, new Nonce(expectedNonce));
        assertThat(hasNonce, is(false));
    }

    @Test
    public void shouldReturnFalseIfNonceValueIsNull() throws Exception {
        String expectedNonce = null;

        boolean hasNonce = nonceValidation.hasNonce(ACCESS_TOKEN_WITH_NONCE, new Nonce(expectedNonce));
        assertThat(hasNonce, is(false));
    }

    @Test
    public void shouldReturnFalseIfNonceIsNull() throws Exception {
        String expectedNonce = null;

        boolean hasNonce = nonceValidation.hasNonce(ACCESS_TOKEN_WITH_NONCE, new Nonce(expectedNonce));
        assertThat(hasNonce, is(false));
    }
}
