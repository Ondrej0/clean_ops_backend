package com.example.backend_clean_ops.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "checklist_items")
public class ChecklistItem extends BaseEntity {
}
