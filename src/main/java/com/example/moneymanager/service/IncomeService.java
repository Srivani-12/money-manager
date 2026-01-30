package com.example.moneymanager.service;

import com.example.moneymanager.dto.IncomeDTO;
import com.example.moneymanager.entity.CategoryEntity;
import com.example.moneymanager.entity.IncomeEntity;
import com.example.moneymanager.entity.ProfileEntity;

import com.example.moneymanager.repository.CategoryRepository;
import com.example.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class IncomeService {


    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;


    private IncomeEntity toEntity(IncomeDTO incomeDTO, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .icon(incomeDTO.getIcon())
                .amount(incomeDTO.getAmount())
                .name(incomeDTO.getName())
                .date(incomeDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private IncomeDTO toDTO(IncomeEntity incomeEntity) {
        return IncomeDTO.builder()
                .id(incomeEntity.getId())
                .icon(incomeEntity.getIcon())
                .amount(incomeEntity.getAmount())
                .name(incomeEntity.getName())
                .date(incomeEntity.getDate())
                .categoryId(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getId() : null)
                .categoryName(incomeEntity.getName() != null ? incomeEntity.getCategory().getName() : null)
                .createdAt(incomeEntity.getCreatedAt())
                .updatedAt(incomeEntity.getUpdatedAt())
                .build();
    }

    public IncomeDTO addIncome(IncomeDTO incomeDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(incomeDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + incomeDTO.getCategoryId()));

        IncomeEntity newExpense = toEntity(incomeDTO, profile, category);
        newExpense = incomeRepository.save(newExpense);
        return toDTO(newExpense);


    }


    public List<IncomeDTO> getCurrentMonthIncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> incomes = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return incomes.stream().map(this::toDTO).toList();


    }


    public void deleteIncome(Long incomeId){
            ProfileEntity profile = profileService.getCurrentProfile();
            IncomeEntity existingIncome = incomeRepository.findById(incomeId)
                    .orElseThrow(() -> new RuntimeException("Income not found with id: " + incomeId));
            if(!existingIncome.getProfile().getId().equals(profile.getId())) {
                throw new RuntimeException("You are not authorized to delete this income.");
            }
            incomeRepository.delete(existingIncome);
        }

        public BigDecimal getTotalIncomesForCurrentUser(){
                ProfileEntity profile = profileService.getCurrentProfile();
                BigDecimal totalIncomes = incomeRepository.findTotalExpenseByProfileId(profile.getId());
                return totalIncomes != null ? totalIncomes : BigDecimal.ZERO;
        }



        public List<IncomeDTO> getLatest5IncomesForCurrentUser(){
                ProfileEntity profile = profileService.getCurrentProfile();
                List<IncomeEntity> incomes = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
                return incomes.stream().map(this::toDTO).toList();
        }
        

        public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String name, Sort sort){
                ProfileEntity profile = profileService.getCurrentProfile();
                List<IncomeEntity> incomes = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                        profile.getId(), startDate, endDate, name != null ? name : "",  sort
                );
                return incomes.stream().map(this::toDTO).toList();
            }







}