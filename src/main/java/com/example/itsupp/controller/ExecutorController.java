package com.example.itsupp.controller;

import com.example.itsupp.model.Executor;
import com.example.itsupp.repository.ExecutorRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/executors")
public class ExecutorController {

    private final ExecutorRepository executorRepository;

    public ExecutorController(ExecutorRepository executorRepository) {
        this.executorRepository = executorRepository;
    }

    @GetMapping
    public List<Executor> getAllExecutors() {
        return executorRepository.findAll();
    }

    @PostMapping
    public Executor createExecutor(@RequestBody Executor executor) {
        return executorRepository.save(executor);
    }

    @PutMapping("/{id}")
    public Executor updateExecutor(@PathVariable Long id, @RequestBody Executor updatedExecutor) {
        return executorRepository.findById(id)
                .map(executor -> {
                    executor.setName(updatedExecutor.getName());
                    executor.setEmail(updatedExecutor.getEmail());
                    executor.setDepartment(updatedExecutor.getDepartment());
                    return executorRepository.save(executor);
                })
                .orElseThrow(() -> new RuntimeException("Executor not found"));
    }

    @DeleteMapping("/{id}")
    public void deleteExecutor(@PathVariable Long id) {
        executorRepository.deleteById(id);
    }
}
