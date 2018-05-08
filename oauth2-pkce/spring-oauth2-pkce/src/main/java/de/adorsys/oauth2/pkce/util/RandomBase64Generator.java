package de.adorsys.oauth2.pkce.util;

import de.adorsys.oauth2.pkce.basetypes.ByteArray;

public class RandomBase64Generator {

    private static final RandomBytesGenerator randomBytesGenerator = new RandomBytesGenerator();
    private static final Base64Encoder base64Encoder = new Base64Encoder();

    public static String generate(int bytesLength) {
        ByteArray stateBytes = randomBytesGenerator.generate(bytesLength);
        return base64Encoder.toBase64(stateBytes);
    }
}
