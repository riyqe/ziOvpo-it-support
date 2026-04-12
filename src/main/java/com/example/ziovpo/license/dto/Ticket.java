package com.example.ziovpo.license.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


public class Ticket {
    
    
    private LocalDateTime serverTime;
    
    
    private long ticketLifetimeSeconds;
    
    
    private LocalDate firstActivationDate;
    
    
    private LocalDate expirationDate;
    
    
    private UUID userId;
    
    
    private UUID deviceId;
    
    
    private boolean blocked;
    public Ticket() {}

    public Ticket(LocalDateTime serverTime, long ticketLifetimeSeconds,
                  LocalDate firstActivationDate, LocalDate expirationDate,
                  UUID userId, UUID deviceId, boolean blocked) {
        this.serverTime = serverTime;
        this.ticketLifetimeSeconds = ticketLifetimeSeconds;
        this.firstActivationDate = firstActivationDate;
        this.expirationDate = expirationDate;
        this.userId = userId;
        this.deviceId = deviceId;
        this.blocked = blocked;
    }
    public LocalDateTime getServerTime() {
        return serverTime;
    }

    public void setServerTime(LocalDateTime serverTime) {
        this.serverTime = serverTime;
    }

    public long getTicketLifetimeSeconds() {
        return ticketLifetimeSeconds;
    }

    public void setTicketLifetimeSeconds(long ticketLifetimeSeconds) {
        this.ticketLifetimeSeconds = ticketLifetimeSeconds;
    }

    public LocalDate getFirstActivationDate() {
        return firstActivationDate;
    }

    public void setFirstActivationDate(LocalDate firstActivationDate) {
        this.firstActivationDate = firstActivationDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
