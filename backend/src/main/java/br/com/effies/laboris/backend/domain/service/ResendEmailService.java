package br.com.effies.laboris.backend.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class ResendEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);

    @Value("${api.resend.key:}")
    private String resendApiKey;

    private final HttpClient httpClient;

    public ResendEmailService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    // Construtor para testes unitários
    public ResendEmailService(HttpClient httpClient, String resendApiKey) {
        this.httpClient = httpClient;
        this.resendApiKey = resendApiKey;
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String userName, String tempPassword, String roleName) {
        String roleDescription = "MANAGER".equalsIgnoreCase(roleName) ? "Gestor" : "Funcionário";
        String subject = "Bem-vindo ao Laboris - Sua Senha Temporária";

        String htmlBody = String.format(
            "<div style=\"font-family: Arial, sans-serif; padding: 20px; color: #333;\">" +
            "<h2>Olá, %s!</h2>" +
            "<p>Sua conta de <strong>%s</strong> no Laboris foi criada com sucesso.</p>" +
            "<p>Use a seguinte senha temporária para realizar seu primeiro login:</p>" +
            "<div style=\"background: #f4f4f4; padding: 10px 15px; border-radius: 5px; font-size: 18px; font-weight: bold; letter-spacing: 1px; display: inline-block;\">%s</div>" +
            "<p style=\"color: #d9534f; margin-top: 15px;\"><strong>Atenção:</strong> Por motivos de segurança, você deverá alterar esta senha no seu primeiro acesso.</p>" +
            "</div>",
            userName, roleDescription, tempPassword
        );

        if (resendApiKey == null || resendApiKey.trim().isEmpty() || "mock".equalsIgnoreCase(resendApiKey.trim())) {
            logWelcomeEmailToConsole(toEmail, subject, htmlBody);
            return;
        }

        try {
            // Escapando as aspas duplas do htmlBody para o payload JSON
            String escapedHtml = htmlBody.replace("\"", "\\\"");

            String jsonPayload = String.format(
                "{\"from\":\"Laboris <onboarding@resend.dev>\",\"to\":[\"%s\"],\"subject\":\"%s\",\"html\":\"%s\"}",
                toEmail,
                subject,
                escapedHtml
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + resendApiKey.trim())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("E-mail de boas-vindas enviado com sucesso para {} via Resend. Response: {}", toEmail, response.body());
            } else {
                log.error("Falha ao enviar e-mail via Resend para {}. Status: {}, Body: {}", toEmail, response.statusCode(), response.body());
                // Fallback para o console
                logWelcomeEmailToConsole(toEmail, subject, htmlBody);
            }
        } catch (Exception e) {
            log.error("Erro ao fazer requisição para a API do Resend: {}", e.getMessage(), e);
            // Fallback para o console
            logWelcomeEmailToConsole(toEmail, subject, htmlBody);
        }
    }

    private void logWelcomeEmailToConsole(String toEmail, String subject, String htmlBody) {
        System.out.println("======================================================================");
        System.out.println("[EMAIL MOCK - RESEND SIMULATOR]");
        System.out.println("Para: " + toEmail);
        System.out.println("Assunto: " + subject);
        System.out.println("HTML:");
        System.out.println(htmlBody);
        System.out.println("======================================================================");
    }
}
