package com.example.system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "sys_user_role")
@IdClass(UserRoleId.class)
@Getter
@Setter
@NoArgsConstructor
public class UserRole {

    @Id
    private Long userId;

    @Id
    private Long roleId;

    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }
}
