package de.adorsys.oauth2.pkce.basetypes;

import org.adorsys.cryptoutils.basetypes.BaseTypeString;

public class Nonce extends BaseTypeString {
    public Nonce(String value) {
        super(value);
    }
}
