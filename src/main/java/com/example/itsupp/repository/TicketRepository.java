package com.example.itsupp.repository;

import com.example.itsupp.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByExecutorId(Long executorId);
}
