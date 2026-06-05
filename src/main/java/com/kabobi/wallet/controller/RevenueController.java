package com.kabobi.wallet.controller;

import com.kabobi.wallet.dto.RevenueDTO;
import com.kabobi.wallet.service.RevenueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/revenues")
@CrossOrigin("*")
public class RevenueController {

    @Autowired
    private RevenueService revenueService;

    @PostMapping
    public ResponseEntity<RevenueDTO> createRevenue(@Valid @RequestBody RevenueDTO revenueDTO) {
        RevenueDTO createdRevenue = revenueService.createRevenue(revenueDTO);
        return new ResponseEntity<>(createdRevenue, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RevenueDTO>> getAllRevenues() {
        List<RevenueDTO> revenues = revenueService.getAllRevenues();
        return ResponseEntity.ok(revenues);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RevenueDTO> getRevenueById(@PathVariable Long id) {
        RevenueDTO revenue = revenueService.getRevenueById(id);
        return ResponseEntity.ok(revenue);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RevenueDTO> updateRevenue(@PathVariable Long id, @Valid @RequestBody RevenueDTO revenueDTO) {
        RevenueDTO updatedRevenue = revenueService.updateRevenue(id, revenueDTO);
        return ResponseEntity.ok(updatedRevenue);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRevenue(@PathVariable Long id) {
        revenueService.deleteRevenue(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<RevenueDTO>> searchRevenues(@RequestParam String keyword) {
        List<RevenueDTO> revenues = revenueService.searchRevenues(keyword);
        return ResponseEntity.ok(revenues);
    }
}