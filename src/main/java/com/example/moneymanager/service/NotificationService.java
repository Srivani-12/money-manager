package com.example.moneymanager.service;

import com.example.moneymanager.dto.ExpenseDTO;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;


    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

//    @Scheduled(cron = "0 0 8 * * ?") // Every day at 8 AM
@Scheduled(cron = "0 0 8 * * ?")
public void sendDailyIncomeExpenseRemainder(){

        log.info("Job started: sendDailyIncomeExpenseRemainder()");
        List<ProfileEntity> profiles = profileRepository.findAll();

        for(ProfileEntity profile: profiles){
            String body = "Hi " + profile.getFullName() + ", <br><br>"
                    + "This is a friendly reminder to log your income and expenses for today. "
                    + "Keeping track of your finances is essential for effective money management. <br><br>"
                    + "You can log your income and expenses by visiting the following link: <br>"
                    + "<a href=\"" + frontendUrl + "\">Money Manager App</a> <br><br>"
                    + "Best regards, <br>"
                    + "Money Manager Team";

            emailService.sendEmail(profile.getEmail(),"Daily Income and Expense Reminder", body);

        }
        log.info("Job finished: sendDailyIncomeExpenseRemainder()");


    }

    @Scheduled(cron = "0 0 22 * * ?") // Every day at 8:30 AM
    public void sendDailyExpenseSummary() {

        log.info("Job started: sendDailyExpenseSummary()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile: profiles) {
            List<ExpenseDTO> todaysExpenses = expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now(ZoneId.of("Asia/Kolkata")));
            if(!todaysExpenses.isEmpty()){

                StringBuilder table = new StringBuilder();
                table.append("<table border='1' style='border-collapse: collapse;'>");
                table.append("<tr><th>Expense Name</th><th>Category</th><th>Amount</th><th>Date</th></tr>");
                for (ExpenseDTO expense : todaysExpenses) {
                    table.append("<tr>")
                            .append("<td>").append(expense.getName()).append("</td>")
                            .append("<td>").append(expense.getCategoryName()).append("</td>")
                            .append("<td>").append(expense.getAmount()).append("</td>")
                            .append("<td>").append(expense.getDate()).append("</td>")
                            .append("</tr>");
                }
                table.append("</table>");

                String body = "Hi " + profile.getFullName() + ", <br><br>"
                        + "Here is the summary of your expenses for today: <br><br>"
                        + table.toString()
                        + "<br>Best regards, <br>"
                        + "Money Manager Team";


            emailService.sendEmail(profile.getEmail(),"Daily Expense Summary", body);
        }

    }




    }




}
