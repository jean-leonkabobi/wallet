package com.kabobi.wallet.controller;

import com.kabobi.wallet.dto.TransactionDTO;
import com.kabobi.wallet.dto.TransactionSearchDTO;
import com.kabobi.wallet.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> searchTransactions(@RequestBody TransactionSearchDTO searchDTO) {
        List<TransactionDTO> transactions = searchService.searchTransactions(searchDTO);
        return ResponseEntity.ok(transactions);
    }
}