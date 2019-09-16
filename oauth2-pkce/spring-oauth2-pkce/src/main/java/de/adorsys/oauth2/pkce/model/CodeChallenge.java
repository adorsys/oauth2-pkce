package de.adorsys.oauth2.pkce.model;

import de.adorsys.sts.cryptoutils.BaseTypeString;

public class CodeChallenge extends BaseTypeString {
    public CodeChallenge(String value) {
        super(value);
    }
}
