package com.drjcodes.subadvise_api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drjcodes.subadvise_api.model.Target;

public interface TargetRepository extends JpaRepository<Target, UUID> {
}
