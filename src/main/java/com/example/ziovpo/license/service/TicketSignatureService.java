package com.example.ziovpo.license.service;

import java.security.Signature;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.example.ziovpo.signature.CanonicalizationService;
import com.example.ziovpo.signature.KeyProvider;
import com.example.ziovpo.signature.SigningService;
import com.example.ziovpo.signature.SignatureProperties;
import com.example.ziovpo.signature.VerificationInfo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TicketSignatureService {

    private final SigningService signingService;
    private final CanonicalizationService canonicalizationService;
    private final KeyProvider keyProvider;
    private final SignatureProperties properties;

    public TicketSignatureService(
            SigningService signingService,
            CanonicalizationService canonicalizationService,
            KeyProvider keyProvider,
            SignatureProperties properties) {
        this.signingService = signingService;
        this.canonicalizationService = canonicalizationService;
        this.keyProvider = keyProvider;
        this.properties = properties;
    }

    public String signTicket(Object ticket) {
        try {
            return signingService.sign(ticket);
        } catch (Exception e) {
            log.error("Error signing ticket", e);
            throw new RuntimeException("Failed to sign ticket", e);
        }
    }

    public boolean verifyTicketSignature(Object ticket, String signatureBase64) {
        try {
            byte[] canonicalBytes = canonicalizationService.canonicalize(ticket);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            VerificationInfo verificationInfo = keyProvider.getVerificationInfo();
            Signature signature = Signature.getInstance(properties.getAlgorithm());
            signature.initVerify(verificationInfo.publicKey());
            signature.update(canonicalBytes);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Error verifying ticket signature", e);
            return false;
        }
    }
}
