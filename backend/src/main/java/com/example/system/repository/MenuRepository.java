package com.example.system.repository;

import com.example.system.entity.Menu;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByStatus(Integer status, Sort sort);

    boolean existsByParentId(Long parentId);

    @Query("""
            select distinct m from Menu m
            join RoleMenu rm on rm.menuId = m.id
            where rm.roleId in (select ur.roleId from UserRole ur where ur.userId = :userId)
              and m.status = 1
            order by m.sort asc
            """)
    List<Menu> findByUserId(@Param("userId") Long userId);

    @Query("""
            select distinct m.perms from Menu m
            join RoleMenu rm on rm.menuId = m.id
            where rm.roleId in :roleIds and m.perms is not null
            """)
    List<String> findPermsByRoleIds(@Param("roleIds") Collection<Long> roleIds);
}
