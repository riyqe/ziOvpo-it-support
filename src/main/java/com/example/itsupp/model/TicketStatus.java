package com.example.itsupp.model;

public enum TicketStatus {
    CREATED,            // создан
    IN_PROGRESS,        // в работе
    NEEDS_INFO,         // требуется информация
    ESCALATED,          // эскалирован
    RESOLVED,           // решён
    SOLVED_BY_USER,     // решён пользователем
    NOT_TO_BE_FIXED     // не будет исправлено
}
