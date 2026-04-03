package com.example.itsupp.signature;

public interface CanonicalizationService {

    byte[] canonicalize(Object payload);
}
