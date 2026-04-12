package com.example.ziovpo.signature;

import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class SigningService {

    private final CanonicalizationService canonicalizationService;
    private final KeyProvider keyProvider;
    private final SignatureProperties properties;

    public SigningService(
            CanonicalizationService canonicalizationService,
            KeyProvider keyProvider,
            SignatureProperties properties) {
        this.canonicalizationService = canonicalizationService;
        this.keyProvider = keyProvider;
        this.properties = properties;
    }

    public String sign(Object payload) {
        byte[] canonicalBytes;
        try {
            canonicalBytes = canonicalizationService.canonicalize(payload);
        } catch (SignatureModuleException e) {
            throw new SignatureModuleException(SignatureErrorCode.CANONICALIZATION_ERROR, "canonicalization error", e);
        }

        PrivateKey signingKey;
        try {
            signingKey = keyProvider.getSigningKey();
        } catch (SignatureModuleException e) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_PROVIDER_ERROR, "key provider error", e);
        }

        try {
            Signature signature = Signature.getInstance(properties.getAlgorithm());
            signature.initSign(signingKey);
            signature.update(canonicalBytes);
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new SignatureModuleException(SignatureErrorCode.SIGN_OPERATION_FAILED, "sign operation failed", e);
        }
    }
}
