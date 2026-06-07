package br.com.effies.laboris.backend.presentation.controller;

import br.com.effies.laboris.backend.domain.entity.Company;
import br.com.effies.laboris.backend.domain.entity.SalaryHistory;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.service.EmployeeService;
import br.com.effies.laboris.backend.domain.service.TokenService;
import br.com.effies.laboris.backend.domain.repository.UserRepository;
import br.com.effies.laboris.backend.presentation.dto.request.CreateSalaryHistoryRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserRepository userRepository;

    private User mockManager;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        Company company = new Company();
        company.setId(UUID.randomUUID());

        mockManager = new User();
        mockManager.setId(UUID.randomUUID());
        mockManager.setEmail("manager@company.com");
        mockManager.setRole(br.com.effies.laboris.backend.domain.entity.enums.UserRole.MANAGER);
        mockManager.setCompany(company);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockManager, null, mockManager.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return 200 OK and list of salaries when GET /employees/{id}/salaries is called")
    void getEmployeeSalaries_ShouldReturnList() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        SalaryHistory history = new SalaryHistory();
        history.setId(UUID.randomUUID());
        history.setHourlyRate(new BigDecimal("20.50"));
        history.setEffectiveDate(LocalDate.of(2026, 6, 1));
        history.setCreateAt(Instant.now());

        when(employeeService.findSalariesByEmployee(eq(employeeId), any(User.class)))
                .thenReturn(List.of(history));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees/" + employeeId + "/salaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hourlyRate").value(20.50))
                .andExpect(jsonPath("$[0].effectiveDate").value("2026-06-01"));

        verify(employeeService, times(1)).findSalariesByEmployee(eq(employeeId), any(User.class));
    }

    @Test
    @DisplayName("Should return 201 Created and new salary when POST /employees/{id}/salaries is called with valid DTO")
    void addEmployeeSalary_WithValidDto_ShouldReturnCreated() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        CreateSalaryHistoryRequestDto requestDto = new CreateSalaryHistoryRequestDto();
        requestDto.setHourlyRate(new BigDecimal("30.00"));
        requestDto.setEffectiveDate(LocalDate.of(2026, 7, 1));

        SalaryHistory history = new SalaryHistory();
        history.setId(UUID.randomUUID());
        history.setHourlyRate(new BigDecimal("30.00"));
        history.setEffectiveDate(LocalDate.of(2026, 7, 1));
        history.setCreateAt(Instant.now());

        when(employeeService.addSalaryHistory(eq(employeeId), any(CreateSalaryHistoryRequestDto.class), any(User.class)))
                .thenReturn(history);

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees/" + employeeId + "/salaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hourlyRate").value(30.00))
                .andExpect(jsonPath("$.effectiveDate").value("2026-07-01"));

        verify(employeeService, times(1))
                .addSalaryHistory(eq(employeeId), any(CreateSalaryHistoryRequestDto.class), any(User.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when POST /employees/{id}/salaries is called with negative rate")
    void addEmployeeSalary_WithNegativeRate_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        CreateSalaryHistoryRequestDto requestDto = new CreateSalaryHistoryRequestDto();
        requestDto.setHourlyRate(new BigDecimal("-5.00")); // Negative rate!
        requestDto.setEffectiveDate(LocalDate.of(2026, 7, 1));

        // Act & Assert
        mockMvc.perform(post("/api/v1/employees/" + employeeId + "/salaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(employeeService);
    }
}
