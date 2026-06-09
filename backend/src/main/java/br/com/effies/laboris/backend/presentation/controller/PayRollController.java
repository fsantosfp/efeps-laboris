package br.com.effies.laboris.backend.presentation.controller;

import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.service.PayrollService;
import br.com.effies.laboris.backend.presentation.dto.request.SettlePayrollRequestDto;
import br.com.effies.laboris.backend.presentation.dto.response.MyPayrollResponseDto;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PayRollController {

    private final PayrollService payrollService;

    PayRollController(PayrollService payrollService){
        this.payrollService = payrollService;
    }

    @GetMapping("/my-payroll")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<MyPayrollResponseDto> getMyPayroll(
        @AuthenticationPrincipal User employee,
        @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
        @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ){
     MyPayrollResponseDto response = payrollService.calculateEmployeePayroll(employee, start, end);
     return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/payroll/settle")
    public ResponseEntity<?> settlePayroll(
        @AuthenticationPrincipal User manager,
        @Valid @RequestBody SettlePayrollRequestDto request
    ){
        payrollService.settlePayroll(request, manager);
        return ResponseEntity.ok(Map.of("message", "Folha de pagamento fechada com sucesso."));
    }
}
