package com.example.itsupp.signature;

import java.security.PrivateKey;

public interface KeyProvider {

    PrivateKey getSigningKey();

    VerificationInfo getVerificationInfo();
}
