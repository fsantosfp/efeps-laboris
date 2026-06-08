package br.com.effies.laboris.backend.presentation.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EmployeeJourneyResponseDto {
    private UUID employeeId;
    private String employeeName;
    private List<JourneyEventDto> events;
}
