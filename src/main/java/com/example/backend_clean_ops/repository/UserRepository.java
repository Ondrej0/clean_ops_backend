package com.example.backend_clean_ops.repository;

import com.example.backend_clean_ops.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
