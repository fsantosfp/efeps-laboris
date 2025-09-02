package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.Job;
import br.com.effies.laboris.backend.domain.entity.JobAssignment;
import br.com.effies.laboris.backend.domain.entity.User;
import br.com.effies.laboris.backend.domain.entity.enums.JobStatus;
import br.com.effies.laboris.backend.domain.repository.JobAssignmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssignmentService {

    private final JobAssignmentRepository repository;

    public AssignmentService( JobAssignmentRepository jobAssignmentRepository){
        this.repository = jobAssignmentRepository;
    }

    public List<Job> findMyActiveAssignments(User employee){
        return repository.findByUserIdAndJobStatus(employee.getId(), JobStatus.IN_PROGRESS)
            .stream()
            .map(JobAssignment::getJob)
            .toList();
    }
}
