package de.adorsys.oauth2.pkce.util;

import de.adorsys.oauth2.pkce.basetypes.ByteArray;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class Base64EncoderTest {

    Base64Encoder base64Encoder;

    @Before
    public void setup() throws Exception {
        base64Encoder = new Base64Encoder();
    }

    @Test
    public void should() throws Exception {
        int[] octets = new int[]{116, 24, 223, 180, 151, 153, 224, 37, 79, 250, 96, 125, 216, 173,
                187, 186, 22, 212, 37, 77, 105, 214, 191, 240, 91, 88, 5, 88, 83,
                132, 141, 121};

        ByteArray byteArray = new ByteArray(octets);

        String base64 = base64Encoder.toBase64(byteArray);

        assertThat(base64, is(equalTo("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk")));
    }

}