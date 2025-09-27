package br.com.effies.laboris.backend.domain.model;

import br.com.effies.laboris.backend.domain.entity.TimeEntry;
import br.com.effies.laboris.backend.domain.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DailyShift(LocalDate date, LocalTime startTime, LocalTime endTime, BigDecimal hoursWorked, User user) { }
