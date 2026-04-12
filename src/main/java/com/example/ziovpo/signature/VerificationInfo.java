package com.example.ziovpo.signature;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

public record VerificationInfo(PublicKey publicKey, X509Certificate certificate) {
}
