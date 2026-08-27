package com.example.system.service.impl;

import com.example.system.common.BusinessException;
import com.example.system.entity.RoleMenu;
import com.example.system.repository.MenuRepository;
import com.example.system.repository.RoleMenuRepository;
import com.example.system.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 角色授权菜单服务单测：不依赖数据库，仓储层全部用 Mockito 替身。
 */
class RoleServiceImplTest {

    private RoleRepository roleRepository;
    private RoleMenuRepository roleMenuRepository;
    private MenuRepository menuRepository;
    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleRepository = org.mockito.Mockito.mock(RoleRepository.class);
        roleMenuRepository = org.mockito.Mockito.mock(RoleMenuRepository.class);
        menuRepository = org.mockito.Mockito.mock(MenuRepository.class);
        roleService = new RoleServiceImpl(roleRepository, roleMenuRepository, menuRepository);
    }

    /** Given 角色存在且菜单均有效 When 全量覆盖授权 Then 删除旧授权并保存去重后的新授权 */
    @Test
    void assignMenus_replacesGrantsWithDistinctRows_whenRoleAndMenusValid() {
        when(roleRepository.findById(5L)).thenReturn(Optional.of(new com.example.system.entity.Role()));
        when(menuRepository.findAllById(anyIterable()))
                .thenReturn(List.of(new com.example.system.entity.Menu(), new com.example.system.entity.Menu()));

        roleService.assignMenus(5L, List.of(1L, 2L, 2L));

        verify(roleMenuRepository).deleteByRoleId(5L);
        verify(roleMenuRepository).saveAll(argThat(rows -> {
            Set<Long> grantedIds = new HashSet<>();
            for (RoleMenu row : rows) {
                if (!Long.valueOf(5L).equals(row.getRoleId())) {
                    return false;
                }
                grantedIds.add(row.getMenuId());
            }
            return grantedIds.equals(Set.of(1L, 2L));
        }));
    }

    /** Given 角色不存在 When 授权 Then 抛出 400 业务异常且不动关联表 */
    @Test
    void assignMenus_throws400AndSkipsWrite_whenRoleMissing() {
        when(roleRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.assignMenus(9L, List.of(1L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("角色不存在")
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(400);
        verifyNoInteractions(roleMenuRepository);
    }

    /** Given 存在无效菜单 ID When 授权 Then 抛 400 且不删除既有授权 */
    @Test
    void assignMenus_throws400WithoutDeleting_whenAnyMenuInvalid() {
        when(roleRepository.findById(5L)).thenReturn(Optional.of(new com.example.system.entity.Role()));
        when(menuRepository.findAllById(anyIterable()))
                .thenReturn(List.of(new com.example.system.entity.Menu()));

        assertThatThrownBy(() -> roleService.assignMenus(5L, List.of(1L, 99L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("存在无效的菜单");
        verify(roleMenuRepository, never()).deleteByRoleId(5L);
    }

    /** Given 空列表 When 授权 Then 仅清空旧授权且不再写入 */
    @Test
    void assignMenus_clearsGrants_whenEmptyListGiven() {
        when(roleRepository.findById(5L)).thenReturn(Optional.of(new com.example.system.entity.Role()));

        roleService.assignMenus(5L, List.of());

        verify(roleMenuRepository).deleteByRoleId(5L);
        verify(roleMenuRepository, never()).saveAll(anyIterable());
    }

    /** Given 角色已有多条授权 When 查询授权菜单 ID Then 返回菜单 ID 列表 */
    @Test
    void getMenuIds_returnsGrantedMenuIds() {
        when(roleRepository.findById(5L)).thenReturn(Optional.of(new com.example.system.entity.Role()));
        when(roleMenuRepository.findByRoleId(5L))
                .thenReturn(List.of(new RoleMenu(5L, 7L), new RoleMenu(5L, 8L)));

        assertThat(roleService.getMenuIds(5L)).containsExactly(7L, 8L);
    }
}
