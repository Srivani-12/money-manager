package com.example.moneymanager.controller;

import com.example.moneymanager.dto.FilterDTO;
import com.example.moneymanager.service.ExpenseService;
import com.example.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/filters")
@RequiredArgsConstructor
public class FilterController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @PostMapping("/filter")
    public ResponseEntity<?> filterTransaction(@RequestBody FilterDTO filter){

        LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.MIN;
        LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
        String keyword = filter.getName() != null ? filter.getName() : "";
        String sortField = filter.getSortField() != null ? filter.getSortField() : "date";
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);

        if("income".equalsIgnoreCase(filter.getType())){
            return ResponseEntity.ok(incomeService.filterIncomes(startDate, endDate, keyword, sort));
        } else if ("expense".equalsIgnoreCase(filter.getType())){
            return ResponseEntity.ok(expenseService.filterExpenses(startDate, endDate, keyword, sort));
        } else {
            return ResponseEntity.badRequest().body("Invalid transaction type. Must be 'income' or 'expense'.");
        }


    }


}
