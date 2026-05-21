package com.example.ziovpo.signature;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class JcsCanonicalizationServiceTest {

    private final JcsCanonicalizationService canonicalizationService = new JcsCanonicalizationService();

    @Test
    void canonicalizesObjectWithSortedKeysAndEscapedStrings() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("text", "a\"b\\c\n");
        payload.put("b", 2);
        payload.put("a", 1);

        byte[] canonical = canonicalizationService.canonicalize(payload);

        assertArrayEquals(
                "{\"a\":1,\"b\":2,\"text\":\"a\\\"b\\\\c\\n\"}".getBytes(StandardCharsets.UTF_8),
                canonical
        );
    }

    @Test
    void canonicalizesRawJsonStringWithStrictDuplicateDetection() {
        byte[] canonical = canonicalizationService.canonicalize("{\"b\":2,\"a\":1}");

        assertArrayEquals(
                "{\"a\":1,\"b\":2}".getBytes(StandardCharsets.UTF_8),
                canonical
        );
    }

    @Test
    void rejectsUnsafeIntegerValues() {
        SignatureModuleException exception = assertThrows(
                SignatureModuleException.class,
                () -> canonicalizationService.canonicalize(Map.of("value", 9_007_199_254_740_992L))
        );

        assertEquals(SignatureErrorCode.INPUT_INVALID, exception.getCode());
    }

    @Test
    void rejectsLoneSurrogates() {
        SignatureModuleException exception = assertThrows(
                SignatureModuleException.class,
                () -> canonicalizationService.canonicalize(Map.of("value", new String(new char[] {'\uD800'})))
        );

        assertEquals(SignatureErrorCode.CANONICALIZATION_FAILED, exception.getCode());
    }
}