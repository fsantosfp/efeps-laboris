package br.com.effies.laboris.backend.presentation.controller;

import br.com.effies.laboris.backend.domain.entity.Displacement;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.service.DisplacementService;
import br.com.effies.laboris.backend.presentation.dto.request.DisplacementRequestDto;
import br.com.effies.laboris.backend.presentation.dto.response.DisplacementResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/displacements")
@PreAuthorize("hasRole('EMPLOYEE')")
public class DisplacementController {

    private final DisplacementService displacementService;

    public DisplacementController(DisplacementService displacementService) {
        this.displacementService = displacementService;
    }

    @PostMapping("/start")
    public ResponseEntity<DisplacementResponseDto> startDisplacement(
            @Valid @RequestBody DisplacementRequestDto request,
            @AuthenticationPrincipal User employee) {

        Displacement displacement = displacementService.startDisplacement(
                request.getLatitude(),
                request.getLongitude(),
                employee
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new DisplacementResponseDto(displacement));
    }

    @PostMapping("/end")
    public ResponseEntity<DisplacementResponseDto> endDisplacement(
            @Valid @RequestBody DisplacementRequestDto request,
            @AuthenticationPrincipal User employee) {

        if (request.getDestinationJobId() == null) {
            throw new IllegalArgumentException("O ID do trabalho de destino é obrigatório para finalizar o deslocamento.");
        }

        Displacement displacement = displacementService.endDisplacement(
                request.getLatitude(),
                request.getLongitude(),
                request.getDestinationJobId(),
                employee
        );
        return ResponseEntity.ok(new DisplacementResponseDto(displacement));
    }

    @GetMapping("/active")
    public ResponseEntity<DisplacementResponseDto> getActiveDisplacement(
            @AuthenticationPrincipal User employee) {

        return displacementService.getActiveDisplacement(employee)
                .map(DisplacementResponseDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/active")
    public ResponseEntity<Void> cancelDisplacement(
            @AuthenticationPrincipal User employee) {
        displacementService.cancelDisplacement(employee);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<List<DisplacementResponseDto>> getMyDisplacements(
            @AuthenticationPrincipal User employee,
            @RequestParam("start") Instant start,
            @RequestParam("end") Instant end) {

        List<Displacement> displacements = displacementService.getMyDisplacements(employee, start, end);
        List<DisplacementResponseDto> response = displacements.stream()
                .map(DisplacementResponseDto::new)
                .toList();
        return ResponseEntity.ok(response);
    }
}
