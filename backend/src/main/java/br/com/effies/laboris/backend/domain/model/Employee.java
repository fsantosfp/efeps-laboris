package br.com.effies.laboris.backend.domain.model;

import br.com.effies.laboris.backend.domain.entity.SalaryHistory;
import br.com.effies.laboris.backend.domain.entity.User;

public record Employee(User user, SalaryHistory salaryHistory) {
}
