package com.example.system.repository;

import com.example.system.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    boolean existsByRoleCode(String roleCode);
}
