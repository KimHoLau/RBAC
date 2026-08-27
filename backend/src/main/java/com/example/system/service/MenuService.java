package com.example.system.service;

import com.example.system.entity.Menu;
import com.example.system.dto.MenuRequest;

import java.util.List;

public interface MenuService {

    /** visible menu tree (directories + menus) for the given user */
    List<Menu> getUserMenus(Long userId);

    /** full menu tree including buttons, for management */
    List<Menu> getFullTree();

    void create(MenuRequest request);

    void update(Long id, MenuRequest request);

    void delete(Long id);
}
