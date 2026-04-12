package com.example.ziovpo.signature;

public interface CanonicalizationService {

    byte[] canonicalize(Object payload);
}
