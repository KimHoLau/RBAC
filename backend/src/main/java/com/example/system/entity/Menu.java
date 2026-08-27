package com.example.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sys_menu")
@Getter
@Setter
@NoArgsConstructor
public class Menu {

    /** directory */
    public static final int TYPE_DIRECTORY = 0;
    /** menu */
    public static final int TYPE_MENU = 1;
    /** button */
    public static final int TYPE_BUTTON = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    @Column(name = "menu_name", nullable = false, length = 50)
    private String menuName;

    /** frontend route path */
    @Column(length = 200)
    private String path;

    /** frontend component path relative to src/views, without ".vue" */
    @Column(length = 200)
    private String component;

    /** permission identifier, e.g. system:user:list */
    @Column(length = 100)
    private String perms;

    @Column(length = 50)
    private String icon;

    /** 0 directory, 1 menu, 2 button */
    @Column(nullable = false)
    private Integer type;

    @Column(nullable = false)
    private Integer sort = 0;

    /** 1 enabled, 0 disabled */
    @Column(nullable = false)
    private Integer status = 1;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /** children assembled by the service layer, never persisted */
    @Transient
    private List<Menu> children = new ArrayList<>();
}
