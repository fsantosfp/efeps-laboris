package br.com.effies.laboris.backend.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResendEmailServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @Test
    @DisplayName("Should use console logging fallback when Resend API Key is empty")
    void shouldFallbackToConsoleWhenApiKeyIsEmpty() throws Exception {
        // Arrange
        ResendEmailService service = new ResendEmailService(httpClient, "");

        // Act
        service.sendWelcomeEmail("test@example.com", "John Doe", "TempPass123", "EMPLOYEE");

        // Assert
        verifyNoInteractions(httpClient);
    }

    @Test
    @DisplayName("Should use console logging fallback when Resend API Key is mock")
    void shouldFallbackToConsoleWhenApiKeyIsMock() throws Exception {
        // Arrange
        ResendEmailService service = new ResendEmailService(httpClient, "mock");

        // Act
        service.sendWelcomeEmail("test@example.com", "John Doe", "TempPass123", "EMPLOYEE");

        // Assert
        verifyNoInteractions(httpClient);
    }

    @Test
    @DisplayName("Should make HTTP POST request when Resend API Key is set")
    @SuppressWarnings("unchecked")
    void shouldMakeHttpRequestWhenApiKeyIsSet() throws Exception {
        // Arrange
        ResendEmailService service = new ResendEmailService(httpClient, "re_123456789");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"id\":\"email_id\"}");

        // Act
        service.sendWelcomeEmail("test@example.com", "John Doe", "TempPass123", "EMPLOYEE");

        // Assert
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        HttpRequest request = requestCaptor.getValue();
        assertThat(request.uri().toString()).isEqualTo("https://api.resend.com/emails");
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.headers().firstValue("Authorization")).contains("Bearer re_123456789");
        assertThat(request.headers().firstValue("Content-Type")).contains("application/json");
    }
}
