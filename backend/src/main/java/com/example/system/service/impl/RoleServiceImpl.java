package com.example.system.service.impl;

import com.example.system.entity.Role;
import com.example.system.repository.RoleRepository;
import com.example.system.service.RoleService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> list() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }
}
