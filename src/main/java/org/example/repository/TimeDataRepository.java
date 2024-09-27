package org.example.repository;

import org.example.entity.TimeStampEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeDataRepository extends JpaRepository<TimeStampEntity, Integer> {
}
