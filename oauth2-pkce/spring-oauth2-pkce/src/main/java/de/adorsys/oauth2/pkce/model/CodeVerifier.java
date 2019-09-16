package de.adorsys.oauth2.pkce.model;

import de.adorsys.sts.cryptoutils.BaseTypeString;

public class CodeVerifier extends BaseTypeString {
    public CodeVerifier(String value) {
        super(value);
    }
}
