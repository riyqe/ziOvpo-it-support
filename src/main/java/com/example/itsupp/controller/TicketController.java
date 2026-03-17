package com.example.itsupp.controller;

import com.example.itsupp.model.*;
import com.example.itsupp.repository.*;
import com.example.itsupp.service.EscalationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final ExecutorRepository executorRepository;
    private final EscalationService escalationService;
    private final SLARepository slaRepository;
    private final CategoryRepository categoryRepository;

    public TicketController(TicketRepository ticketRepository,
                            ExecutorRepository executorRepository,
                            EscalationService escalationService,
                            SLARepository slaRepository,
                            CategoryRepository categoryRepository) {
        this.ticketRepository = ticketRepository;
        this.executorRepository = executorRepository;
        this.escalationService = escalationService;
        this.slaRepository = slaRepository;
        this.categoryRepository = categoryRepository;
    }

    // Получить все тикеты
    @GetMapping
    public List<Ticket> getAll() {
        return ticketRepository.findAll();
    }

    // Получить один тикет
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getById(@PathVariable Long id) {
        return ticketRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Ticket create(@RequestBody Ticket ticket) {
        // базовые поля
        if (ticket.getCreatedAt() == null) {
            ticket.setCreatedAt(LocalDateTime.now());
        }
        if (ticket.getStatus() == null) {
            ticket.setStatus(TicketStatus.CREATED);
        }

        // сейвим только ID категории
        Long categoryId = (ticket.getCategory() != null && ticket.getCategory().getId() != null)
                ? ticket.getCategory().getId() : null;

        // Очищаем связанные объекты
        ticket.setSla(null);
        ticket.setCategory(null);
        ticket.setExecutor(null);

        // загрузка категории из базы
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            ticket.setCategory(category);

            // назнач SLA на основе категории
            SLA sla = determineSLAbyCategory(category);
            ticket.setSla(sla);
        }

        return ticketRepository.save(ticket);
    }

    private SLA determineSLAbyCategory(Category category) {
        Long categoryId = category.getId();

        // по ID категории
        if (categoryId == 1) { // Network -> Critical
            return slaRepository.findById(1L).orElse(null);
        } else if (categoryId == 2) { // Software -> High
            return slaRepository.findById(2L).orElse(null);
        } else if (categoryId == 3) { // Hardware -> Medium
            return slaRepository.findById(3L).orElse(null);
        } else { // остальное -> Low
            return slaRepository.findById(4L).orElse(null);
        }
    }




    // обновление
    @PutMapping("/{id}")
    public ResponseEntity<Ticket> update(@PathVariable Long id, @RequestBody Ticket details) {
        return ticketRepository.findById(id).map(ticket -> {
            if (details.getTitle() != null) ticket.setTitle(details.getTitle());
            if (details.getDescription() != null) ticket.setDescription(details.getDescription());
            if (details.getStatus() != null) ticket.setStatus(details.getStatus());

            // Если меняем SLA или категорию
            if (details.getSla() != null && details.getSla().getId() != null) {
                slaRepository.findById(details.getSla().getId()).ifPresent(ticket::setSla);
            }

            ticket.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok(ticketRepository.save(ticket));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Бизнес-операция 1: Назначить тикет исполнителю
    @PostMapping("/{ticketId}/assign/{executorId}")
    public ResponseEntity<Ticket> assignExecutor(@PathVariable Long ticketId, @PathVariable Long executorId) {
        return ticketRepository.findById(ticketId)
                .flatMap(ticket -> executorRepository.findById(executorId)
                        .map(exec -> {
                            ticket.setExecutor(exec);
                            ticket.setStatus(TicketStatus.IN_PROGRESS);
                            ticket.setUpdatedAt(LocalDateTime.now());
                            return ResponseEntity.ok(ticketRepository.save(ticket));
                        }))
                .orElse(ResponseEntity.notFound().build());
    }

    // Бизнес-операция 2: Закрыть тикет
    @PostMapping("/{id}/resolve")
    public ResponseEntity<Ticket> resolve(@PathVariable Long id, @RequestBody String resolution) {
        return ticketRepository.findById(id).map(ticket -> {
            // меняем: статус и решение
            ticket.setStatus(TicketStatus.RESOLVED);
            ticket.setResolution(resolution);
            ticket.setUpdatedAt(LocalDateTime.now());

            // сейвим тикет. остальные поля (executor, sla, category) останутся как были.
            return ResponseEntity.ok(ticketRepository.save(ticket));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Бизнес-операция 3: Просроченные тикеты
    @GetMapping("/overdue")
    public List<Ticket> getOverdue() {
        return escalationService.getOverdueTickets();
    }

    // Бизнес-операция 4: Эскалировать тикеты
    @PostMapping("/escalate")
    public ResponseEntity<String> escalateAll() {
        // Вызываем логику проверки сроков
        int count = escalationService.escalateOverdueTickets();
        return ResponseEntity.ok("Проверка завершена. Эскалировано тикетов: " + count);
    }

    // Бизнес-операция 5: Получить тикеты конкретного исполнителя
    @GetMapping("/executor/{executorId}")
    public List<Ticket> getTicketsByExecutor(@PathVariable Long executorId) {
        return ticketRepository.findByExecutorId(executorId);
    }
}