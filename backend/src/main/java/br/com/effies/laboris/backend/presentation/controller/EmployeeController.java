package br.com.effies.laboris.backend.presentation.controller;

import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.model.Employee;
import br.com.effies.laboris.backend.domain.service.EmployeeService;
import br.com.effies.laboris.backend.presentation.dto.request.CreateEmployeeRequestDto;
import br.com.effies.laboris.backend.presentation.dto.response.EmployeeResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService){
        this.employeeService = employeeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<EmployeeResponseDto> createEmployee(
        @Valid @RequestBody CreateEmployeeRequestDto request,
        @AuthenticationPrincipal User manager){

        Employee employee = employeeService.create(request, manager);
        EmployeeResponseDto response = new EmployeeResponseDto(employee);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<EmployeeResponseDto>> listEmployees( @AuthenticationPrincipal User manager){

        List<Employee> employees = employeeService.findAllByManager(manager);

        List<EmployeeResponseDto> response = employees.stream()
            .filter(employee -> employee.salaryHistory() != null)
            .map(EmployeeResponseDto::new)
            .toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deactivateEmployee(
        @PathVariable UUID employeeId,
        @AuthenticationPrincipal User manager
    ){
        employeeService.deactivate(employeeId, manager);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{employeeId}/activate")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> activateEmployee(
        @PathVariable UUID employeeId,
        @AuthenticationPrincipal User manager
    ){
        employeeService.activate(employeeId, manager);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{employeeId}/salaries")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<br.com.effies.laboris.backend.presentation.dto.response.SalaryHistoryResponseDto>> getEmployeeSalaries(
            @PathVariable UUID employeeId,
            @AuthenticationPrincipal User manager
    ) {
        List<br.com.effies.laboris.backend.domain.entity.SalaryHistory> salaries = employeeService.findSalariesByEmployee(employeeId, manager);
        List<br.com.effies.laboris.backend.presentation.dto.response.SalaryHistoryResponseDto> response = salaries.stream()
                .map(br.com.effies.laboris.backend.presentation.dto.response.SalaryHistoryResponseDto::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{employeeId}/salaries")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<br.com.effies.laboris.backend.presentation.dto.response.SalaryHistoryResponseDto> addEmployeeSalary(
            @PathVariable UUID employeeId,
            @Valid @RequestBody br.com.effies.laboris.backend.presentation.dto.request.CreateSalaryHistoryRequestDto request,
            @AuthenticationPrincipal User manager
    ) {
        br.com.effies.laboris.backend.domain.entity.SalaryHistory salaryHistory = employeeService.addSalaryHistory(employeeId, request, manager);
        br.com.effies.laboris.backend.presentation.dto.response.SalaryHistoryResponseDto response = new br.com.effies.laboris.backend.presentation.dto.response.SalaryHistoryResponseDto(salaryHistory);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
