package com.runningtracker.repository;

import com.runningtracker.domain.Training;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainingRepository extends JpaRepository<Training, Long> {

    List<Training> findAllByOwnerEmail(String email);

    List<Training> findAllByOwnerEmailOrderByDateDescIdDesc(String email);

    Optional<Training> findByIdAndOwnerEmail(Long id, String email);
}
