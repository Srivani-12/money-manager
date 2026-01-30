package com.example.moneymanager.service;

import com.example.moneymanager.dto.ExpenseDTO;
import com.example.moneymanager.entity.CategoryEntity;
import com.example.moneymanager.entity.ExpenseEntity;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.CategoryRepository;
import com.example.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;


    public ExpenseDTO addExpense(ExpenseDTO expenseDTO){
       ProfileEntity profile = profileService.getCurrentProfile();
       CategoryEntity category = categoryRepository.findById(expenseDTO.getCategoryId())
               .orElseThrow(() -> new RuntimeException("Category not found with id: " + expenseDTO.getCategoryId()));

         ExpenseEntity newExpense = toEntity(expenseDTO, profile, category);
         newExpense = expenseRepository.save(newExpense);
         return toDTO(newExpense);


    }

    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetween(profile.getId(),startDate,endDate);
        return expenses.stream().map(this::toDTO).toList();
    }


    public void deleteExpense(Long expenseId){
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity existingExpense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + expenseId));
        if(!existingExpense.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("You are not authorized to delete this expense.");
        }
        expenseRepository.delete(existingExpense);
    }


    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> expenses = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return expenses.stream().map(this::toDTO).toList();
    }

    public BigDecimal getTotalExpensesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal totalExpenses = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
    }

    //filter expenses

    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String name, Sort sort){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                profile.getId(), startDate, endDate, name != null ? name : "",  sort
        );
        return expenses.stream().map(this::toDTO).toList();
    }

    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId, LocalDate date) {
        List<ExpenseEntity> expenses =
                expenseRepository.findByProfileIdAndDate(profileId, date);

        return expenses.stream().map(this::toDTO).toList();
    }



    private ExpenseEntity toEntity(ExpenseDTO expenseDTO, ProfileEntity profile, CategoryEntity category){
        return ExpenseEntity.builder()
                .icon(expenseDTO.getIcon())
                .amount(expenseDTO.getAmount())
                .name(expenseDTO.getName())
                .date(expenseDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private ExpenseDTO toDTO(ExpenseEntity expenseEntity){
        return ExpenseDTO.builder()
                .id(expenseEntity.getId())
                .icon(expenseEntity.getIcon())
                .amount(expenseEntity.getAmount())
                .name(expenseEntity.getName())
                .date(expenseEntity.getDate())
                .categoryId(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getId() : null)
                .categoryName(expenseEntity.getName() != null ? expenseEntity.getCategory().getName() : null)
                .createdAt(expenseEntity.getCreatedAt())
                .updatedAt(expenseEntity.getUpdatedAt())
                .build();
    }




}
