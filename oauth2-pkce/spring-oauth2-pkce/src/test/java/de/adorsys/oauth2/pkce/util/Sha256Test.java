package de.adorsys.oauth2.pkce.util;

import de.adorsys.oauth2.pkce.model.ByteArray;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class Sha256Test {

    Sha256 sha256;

    @Before
    public void setup() throws Exception {
        sha256 = new Sha256();
    }

    @Test
    public void shouldHash() throws Exception {
        ByteArray expected = new ByteArray(new int[]{19, 211, 30, 150, 26, 26, 216, 236, 47, 22, 177, 12, 76, 152, 46,
                8, 118, 168, 120, 173, 109, 241, 68, 86, 110, 225, 137, 74, 203,
                112, 249, 195});

        ByteArray sha256Bytes = sha256.hashIt("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk");

        assertThat(sha256Bytes, is(equalTo(expected)));
    }
}
