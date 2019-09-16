package de.adorsys.oauth2.pkce.model;

import de.adorsys.sts.cryptoutils.BaseTypeString;

public class Nonce extends BaseTypeString {
    public Nonce(String value) {
        super(value);
    }
}
