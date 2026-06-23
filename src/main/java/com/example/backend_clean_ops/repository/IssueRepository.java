package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {
}
