package com.example.ziovpo.signature;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class JcsCanonicalizationService implements CanonicalizationService {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public byte[] canonicalize(Object payload) {
        if (payload == null) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "payload is null");
        }

        try {
            JsonNode model = objectMapper.valueToTree(payload);
            String canonicalJson = toCanonicalJson(model);
            return canonicalJson.getBytes(StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "payload serialization failed", e);
        } catch (SignatureModuleException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureModuleException(SignatureErrorCode.CANONICALIZATION_FAILED, "canonicalization failed", e);
        }
    }

    private String toCanonicalJson(JsonNode node) {
        StringBuilder sb = new StringBuilder();
        appendNode(node, sb);
        return sb.toString();
    }

    private void appendNode(JsonNode node, StringBuilder sb) {
        if (node == null || node.isNull()) {
            sb.append("null");
            return;
        }

        if (node.isObject()) {
            appendObject(node, sb);
            return;
        }

        if (node.isArray()) {
            appendArray(node, sb);
            return;
        }

        if (node.isTextual()) {
            appendQuoted(node.textValue(), sb);
            return;
        }

        if (node.isNumber()) {
            appendNumber(node, sb);
            return;
        }

        if (node.isBoolean()) {
            sb.append(node.booleanValue());
            return;
        }

        appendQuoted(node.asText(), sb);
    }

    private void appendObject(JsonNode node, StringBuilder sb) {
        sb.append('{');
        List<String> keys = new ArrayList<>();
        Iterator<String> fields = node.fieldNames();
        while (fields.hasNext()) {
            keys.add(fields.next());
        }
        Collections.sort(keys);

        boolean first = true;
        for (String key : keys) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            appendQuoted(key, sb);
            sb.append(':');
            appendNode(node.get(key), sb);
        }
        sb.append('}');
    }

    private void appendArray(JsonNode node, StringBuilder sb) {
        sb.append('[');
        for (int i = 0; i < node.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            appendNode(node.get(i), sb);
        }
        sb.append(']');
    }

    private void appendNumber(JsonNode node, StringBuilder sb) {
        if (node.isFloatingPointNumber()) {
            double value = node.doubleValue();
            if (!Double.isFinite(value)) {
                throw new SignatureModuleException(SignatureErrorCode.INPUT_INVALID, "non-finite numeric value");
            }
            sb.append(node.decimalValue().stripTrailingZeros().toPlainString());
            return;
        }
        sb.append(node.numberValue().toString());
    }

    private void appendQuoted(String value, StringBuilder sb) {
        try {
            sb.append(objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            throw new SignatureModuleException(SignatureErrorCode.OUTPUT_ENCODING_FAILED, "string encoding failed", e);
        }
    }
}
