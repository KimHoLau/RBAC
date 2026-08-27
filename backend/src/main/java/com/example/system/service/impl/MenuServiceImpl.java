package com.example.system.service.impl;

import com.example.system.common.BusinessException;
import com.example.system.dto.MenuRequest;
import com.example.system.entity.Menu;
import com.example.system.repository.MenuRepository;
import com.example.system.repository.RoleMenuRepository;
import com.example.system.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final RoleMenuRepository roleMenuRepository;

    public MenuServiceImpl(MenuRepository menuRepository, RoleMenuRepository roleMenuRepository) {
        this.menuRepository = menuRepository;
        this.roleMenuRepository = roleMenuRepository;
    }

    @Override
    public List<Menu> getUserMenus(Long userId) {
        List<Menu> menus = menuRepository.findByUserId(userId).stream()
                .filter(menu -> menu.getType() != Menu.TYPE_BUTTON)
                .toList();
        return buildTree(menus);
    }

    @Override
    public List<Menu> getFullTree() {
        return buildTree(menuRepository.findAll());
    }

    @Override
    @Transactional
    public void create(MenuRequest request) {
        validateParent(request.getParentId());
        menuRepository.save(fromRequest(request, new Menu()));
    }

    @Override
    @Transactional
    public void update(Long id, MenuRequest request) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(400, "菜单不存在"));
        if (request.getParentId().equals(id)) {
            throw new BusinessException(400, "上级菜单不能是自己");
        }
        validateParent(request.getParentId());
        menuRepository.save(fromRequest(request, menu));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!menuRepository.existsById(id)) {
            throw new BusinessException(400, "菜单不存在");
        }
        if (menuRepository.existsByParentId(id)) {
            throw new BusinessException(400, "存在子菜单，无法删除");
        }
        if (roleMenuRepository.existsByMenuId(id)) {
            throw new BusinessException(400, "菜单已分配给角色，请先解除关联");
        }
        menuRepository.deleteById(id);
    }

    private void validateParent(Long parentId) {
        if (parentId != null && parentId != 0L && !menuRepository.existsById(parentId)) {
            throw new BusinessException(400, "上级菜单不存在");
        }
    }

    private Menu fromRequest(MenuRequest request, Menu menu) {
        menu.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        menu.setMenuName(request.getMenuName());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setPerms(request.getPerms());
        menu.setIcon(request.getIcon());
        menu.setType(request.getType());
        menu.setSort(request.getSort() == null ? 0 : request.getSort());
        menu.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        return menu;
    }

    /** assembles a sorted tree; roots are the nodes whose parentId is 0 */
    private List<Menu> buildTree(List<Menu> menus) {
        Map<Long, List<Menu>> byParent = new LinkedHashMap<>();
        for (Menu menu : menus) {
            byParent.computeIfAbsent(menu.getParentId(), key -> new java.util.ArrayList<>()).add(menu);
        }

        List<Menu> roots = byParent.getOrDefault(0L, List.of()).stream()
                .sorted(byOrder())
                .toList();

        for (Menu root : roots) {
            attachChildren(root, byParent);
        }
        return roots;
    }

    private void attachChildren(Menu parent, Map<Long, List<Menu>> byParent) {
        List<Menu> children = byParent.getOrDefault(parent.getId(), List.of()).stream()
                .sorted(byOrder())
                .toList();
        parent.setChildren(children);
        for (Menu child : children) {
            attachChildren(child, byParent);
        }
    }

    private Comparator<Menu> byOrder() {
        return Comparator.comparing(Menu::getSort, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Menu::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
