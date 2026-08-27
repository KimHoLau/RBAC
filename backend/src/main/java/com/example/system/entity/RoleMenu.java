package com.example.system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sys_role_menu")
@IdClass(RoleMenuId.class)
@Getter
@Setter
@NoArgsConstructor
public class RoleMenu {

    @Id
    private Long roleId;

    @Id
    private Long menuId;

    public RoleMenu(Long roleId, Long menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }
}
