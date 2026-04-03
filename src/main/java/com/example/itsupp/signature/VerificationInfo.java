package com.example.itsupp.signature;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

public record VerificationInfo(PublicKey publicKey, X509Certificate certificate) {
}
