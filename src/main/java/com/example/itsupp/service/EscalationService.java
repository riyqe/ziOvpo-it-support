package com.example.itsupp.service;

import com.example.itsupp.model.Ticket;
import com.example.itsupp.model.TicketStatus;
import com.example.itsupp.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EscalationService {

    private final TicketRepository ticketRepository;
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void runAutoEscalation() {
        log.info("Запуск автоматической проверки SLA");
        int count = escalateOverdueTickets();
        if (count > 0) {
            log.info("Эскалировано тикетов: {}", count);
        } else {
            log.info("Просроченных тикетов нет");
        }
    }

    public int escalateOverdueTickets() {
        List<Ticket> activeTickets = ticketRepository.findAll();
        int counter = 0;

        for (Ticket ticket : activeTickets) {
            if (isTerminalState(ticket.getStatus())) {
                continue;
            }
            if (ticket.getSla() == null) {
                continue;
            }
            int hoursAllowed = ticket.getSla().getResolveHours();
            LocalDateTime deadline = ticket.getCreatedAt().plusHours(hoursAllowed);
            if (LocalDateTime.now().isAfter(deadline)) {
                log.warn("Тикет ID {} просрочен! Дедлайн был: {}. Эскалируем...", ticket.getId(), deadline);

                ticket.setStatus(TicketStatus.ESCALATED);
                ticket.setDescription(ticket.getDescription() + " [AUTO-ESCALATED: SLA Breach]");
                ticket.setUpdatedAt(LocalDateTime.now());

                ticketRepository.save(ticket);
                counter++;
            }
        }
        return counter;
    }
    private boolean isTerminalState(TicketStatus status) {
        return status == TicketStatus.RESOLVED
                || status == TicketStatus.ESCALATED
                || status == TicketStatus.NOT_TO_BE_FIXED
                || status == TicketStatus.SOLVED_BY_USER;
    }
    public List<Ticket> getOverdueTickets() {
        return ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.ESCALATED)
                .toList();
    }
}
