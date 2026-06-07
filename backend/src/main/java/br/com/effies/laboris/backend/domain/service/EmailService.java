package br.com.effies.laboris.backend.domain.service;

public interface EmailService {
    void sendWelcomeEmail(String toEmail, String userName, String tempPassword, String roleName);
}
