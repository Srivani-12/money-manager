package com.example.moneymanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.OutputStream;
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
            URL url = new URL(BREVO_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "application/json");
            conn.setRequestProperty("content-type", "application/json");
            conn.setRequestProperty("api-key", apiKey);
            conn.setDoOutput(true);

            // Using simple JSON manual building, ensuring basic escaping for double quotes
            // Note: For complex content, consider using a library like Jackson or Gson
            String body = String.format(
                    "{\"sender\":{\"email\":\"%s\"},\"to\":[{\"email\":\"%s\"}],\"subject\":\"%s\",\"htmlContent\":\"%s\"}",
                    from,
                    to,
                    escapeJson(subject),
                    escapeJson(content)
            );

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 400) {
                // In a real app, you might read the error stream here for more details
                throw new RuntimeException("Brevo API error: HTTP " + responseCode);
            }

        } catch (Exception e) {
            // Logs are essential for debugging cloud deployments
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    // Prevents JSON from breaking if subject/content contains double quotes
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}