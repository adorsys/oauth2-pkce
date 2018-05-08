package de.adorsys.oauth2.pkce.context;

import de.adorsys.oauth2.pkce.basetypes.*;
import de.adorsys.oauth2.pkce.util.Base64Encoder;
import de.adorsys.oauth2.pkce.util.RandomBase64Generator;
import de.adorsys.oauth2.pkce.util.RandomBytesGenerator;
import de.adorsys.oauth2.pkce.util.Sha256;

public class Oauth2PkceContextFactory {

    private final RandomBytesGenerator randomBytesGenerator = new RandomBytesGenerator();
    private final Base64Encoder base64Encoder = new Base64Encoder();
    private final Sha256 sha256 = new Sha256();


    public Oauth2PkceContext create() {
        Oauth2PkceContext context = new Oauth2PkceContext();

        CodeVerifier codeVerifier = generateCodeVerifier();
        context.setCodeVerifier(codeVerifier);

        CodeChallenge codeChallenge = createCodeChallenge(codeVerifier);
        context.setCodeChallenge(codeChallenge);

        State state = generateState();
        context.setState(state);

        Nonce nonce = generateNonce();
        context.setNonce(nonce);

        return context;
    }

    private CodeVerifier generateCodeVerifier() {
        ByteArray challengeVerifierBytes = randomBytesGenerator.generate(32);
        String codeVerifier = base64Encoder.toBase64(challengeVerifierBytes);

        return new CodeVerifier(codeVerifier);
    }

    private CodeChallenge createCodeChallenge(CodeVerifier codeVerifier) {
        ByteArray codeChallengeBytes = sha256.hashIt(codeVerifier.getValue());
        String codeChallenge = base64Encoder.toBase64(codeChallengeBytes);

        return new CodeChallenge(codeChallenge);
    }

    private State generateState() {
        String base64 = RandomBase64Generator.generate(20);
        return new State(base64);
    }

    private Nonce generateNonce() {
        String nonce = RandomBase64Generator.generate(20);
        return new Nonce(nonce);
    }

}
