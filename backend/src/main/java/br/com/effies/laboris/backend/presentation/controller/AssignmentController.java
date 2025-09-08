package br.com.effies.laboris.backend.presentation.controller;

import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.service.AssignmentService;
import br.com.effies.laboris.backend.presentation.dto.response.MyAssignmentsResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/my-assignments")
@PreAuthorize("hasRole('EMPLOYEE')")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService){
        this.assignmentService = assignmentService;
    }


    @GetMapping
    public ResponseEntity<List<MyAssignmentsResponseDto>> getMyAssignments(@AuthenticationPrincipal User employee){

      List<Job> myAssignments = assignmentService.findMyActiveAssignments(employee);
      List<MyAssignmentsResponseDto> response = myAssignments.stream().map(MyAssignmentsResponseDto::new).toList();

      return ResponseEntity.ok(response);
    }
}
