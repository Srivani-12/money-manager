package com.example.moneymanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String from;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    public void sendEmail(String to, String subject, String content) {
        try {
            HttpURLConnection conn =
                    (HttpURLConnection) new URL(BREVO_URL).openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "application/json");
            conn.setRequestProperty("content-type", "application/json");
            conn.setRequestProperty("api-key", apiKey);
            conn.setDoOutput(true);

            String body = """
            {
              "sender": {"email": "%s"},
              "to": [{"email": "%s"}],
              "subject": "%s",
              "htmlContent": "%s"
            }
            """.formatted(from, to, subject, content);

            conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

            if (conn.getResponseCode() >= 400) {
                throw new RuntimeException("Brevo email failed");
            }

        } catch (Exception e) {
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
