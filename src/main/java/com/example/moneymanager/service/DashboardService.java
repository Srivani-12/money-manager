package com.example.moneymanager.service;

import com.example.moneymanager.dto.ExpenseDTO;
import com.example.moneymanager.dto.IncomeDTO;
import com.example.moneymanager.dto.RecentTransactionDTO;
import com.example.moneymanager.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;


    public Map<String, Object> getDashboardData() {

        ProfileEntity profile = profileService.getCurrentProfile();
        Map<String, Object> dashboardData = new LinkedHashMap<>();

        List<IncomeDTO> lastestIncomes = incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDTO> lastestExpenses = expenseService.getLatest5ExpensesForCurrentUser();

        List<RecentTransactionDTO> recentTransactions = concat(lastestIncomes.stream().map(income ->
                RecentTransactionDTO.builder()
                        .id(income.getId())
                        .profileId(profile.getId())
                        .icon(income.getIcon())
                        .name(income.getName())
                        .amount(income.getAmount())
                        .date(income.getDate())
                        .createdAt(income.getCreatedAt())
                        .updatedAt(income.getUpdatedAt())
                        .type("income")
                        .build()),
                lastestExpenses.stream().map(expense ->
                RecentTransactionDTO.builder()
                        .id(expense.getId())
                        .profileId(profile.getId())
                        .icon(expense.getIcon())
                        .name(expense.getName())
                        .amount(expense.getAmount())
                        .date(expense.getDate())
                        .createdAt(expense.getCreatedAt())
                        .updatedAt(expense.getUpdatedAt())
                        .type("expense")
                        .build()
        )).sorted((t1, t2) -> {
            int cmp = t2.getDate().compareTo(t1.getDate());
            if (cmp == 0 && t1.getCreatedAt() != null && t2.getCreatedAt() != null) {
                return t2.getCreatedAt().compareTo(t1.getCreatedAt());
            }
            return cmp;
        }).collect(Collectors.toList());

        dashboardData.put("totalBalance",incomeService.getTotalIncomesForCurrentUser().subtract(expenseService.getTotalExpensesForCurrentUser()));
        dashboardData.put("totalIncomes", incomeService.getTotalIncomesForCurrentUser());
        dashboardData.put("totalExpenses", expenseService.getTotalExpensesForCurrentUser());
        dashboardData.put("recent5Expenses", lastestExpenses);
        dashboardData.put("recent5Incomes", lastestIncomes);
        dashboardData.put("recentTransactions", recentTransactions.stream().limit(5).collect(Collectors.toList()));

        return dashboardData;


    }



}
