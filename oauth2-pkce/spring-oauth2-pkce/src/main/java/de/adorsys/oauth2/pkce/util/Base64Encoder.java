package de.adorsys.oauth2.pkce.util;

import de.adorsys.oauth2.pkce.basetypes.ByteArray;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Base64Encoder {

    public String toBase64(ByteArray byteArray) {
        return toBase64(byteArray.getValue());
    }

    private String toBase64(byte[] bytes) {
        byte[] encodedBase64InBytes = Base64.getEncoder().encode(bytes);

        String base64AsString = new String(encodedBase64InBytes);

        base64AsString = base64AsString.split("=")[0];
        base64AsString = base64AsString.replace('+', '-');
        base64AsString = base64AsString.replace('/', '_');

        return base64AsString;
    }

}
