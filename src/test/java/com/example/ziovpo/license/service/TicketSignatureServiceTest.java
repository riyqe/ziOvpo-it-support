package com.example.ziovpo.license.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.ziovpo.license.dto.Ticket;
import com.example.ziovpo.signature.CanonicalizationService;
import com.example.ziovpo.signature.JcsCanonicalizationService;
import com.example.ziovpo.signature.KeyProvider;
import com.example.ziovpo.signature.SignatureProperties;
import com.example.ziovpo.signature.SigningService;
import com.example.ziovpo.signature.VerificationInfo;

class TicketSignatureServiceTest {

    private TicketSignatureService ticketSignatureService;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        SignatureProperties properties = new SignatureProperties();
        properties.setAlgorithm("SHA256withRSA");

        CanonicalizationService canonicalizationService = new JcsCanonicalizationService();
        KeyProvider inMemoryKeyProvider = new InMemoryKeyProvider(keyPair);
        SigningService signingService = new SigningService(canonicalizationService, inMemoryKeyProvider, properties);

        ticketSignatureService = new TicketSignatureService(
                signingService,
                canonicalizationService,
                inMemoryKeyProvider,
                properties
        );
    }

    @Test
    void signAndVerifyTicketSuccess() {
        Ticket ticket = createTicket();

        String signature = ticketSignatureService.signTicket(ticket);

        assertNotNull(signature);
        assertTrue(ticketSignatureService.verifyTicketSignature(ticket, signature));
    }

    @Test
    void verifyShouldFailAfterTicketTampering() {
        Ticket original = createTicket();
        String signature = ticketSignatureService.signTicket(original);

        Ticket tampered = createTicket();
        tampered.setBlocked(true);

        assertFalse(ticketSignatureService.verifyTicketSignature(tampered, signature));
    }

    @Test
    void verifyShouldFailForInvalidSignatureValue() {
        Ticket ticket = createTicket();

        assertFalse(ticketSignatureService.verifyTicketSignature(ticket, "not-a-base64-signature"));
    }

    private Ticket createTicket() {
        Ticket ticket = new Ticket();
        ticket.setServerTime(LocalDateTime.of(2026, 4, 13, 12, 0, 0));
        ticket.setTicketLifetimeSeconds(3600);
        ticket.setFirstActivationDate(LocalDate.of(2026, 4, 1));
        ticket.setExpirationDate(LocalDate.of(2026, 5, 1));
        ticket.setUserId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        ticket.setDeviceId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        ticket.setBlocked(false);
        return ticket;
    }

    private static final class InMemoryKeyProvider implements KeyProvider {

        private final KeyPair keyPair;

        private InMemoryKeyProvider(KeyPair keyPair) {
            this.keyPair = keyPair;
        }

        @Override
        public java.security.PrivateKey getSigningKey() {
            return keyPair.getPrivate();
        }

        @Override
        public VerificationInfo getVerificationInfo() {
            return new VerificationInfo(keyPair.getPublic(), null);
        }
    }
}
