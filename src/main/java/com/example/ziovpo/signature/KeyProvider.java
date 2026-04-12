package com.example.ziovpo.signature;

import java.security.PrivateKey;

public interface KeyProvider {

    PrivateKey getSigningKey();

    VerificationInfo getVerificationInfo();
}
