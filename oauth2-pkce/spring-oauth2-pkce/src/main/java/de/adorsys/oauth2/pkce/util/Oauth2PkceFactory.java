package de.adorsys.oauth2.pkce.util;

import de.adorsys.oauth2.pkce.model.*;

public class Oauth2PkceFactory {
    private static final int CODE_CHALLENGE_SIZE = 32;
    private static final int STATE_SIZE = 20;
    private static final int NONCE_SIZE = 20;

    private final RandomBytesGenerator randomBytesGenerator = new RandomBytesGenerator();
    private final Base64Encoder base64Encoder = new Base64Encoder();
    private final Sha256 sha256 = new Sha256();

    public CodeVerifier generateCodeVerifier() {
        ByteArray challengeVerifierBytes = randomBytesGenerator.generate(CODE_CHALLENGE_SIZE);
        String codeVerifier = base64Encoder.toBase64(challengeVerifierBytes);

        return new CodeVerifier(codeVerifier);
    }

    public CodeChallenge createCodeChallenge(CodeVerifier codeVerifier) {
        ByteArray codeChallengeBytes = sha256.hashIt(codeVerifier.getValue());
        String codeChallenge = base64Encoder.toBase64(codeChallengeBytes);

        return new CodeChallenge(codeChallenge);
    }

    public State generateState() {
        String base64 = RandomBase64Generator.generate(STATE_SIZE);
        return new State(base64);
    }

    public Nonce generateNonce() {
        String nonce = RandomBase64Generator.generate(NONCE_SIZE);
        return new Nonce(nonce);
    }
}
