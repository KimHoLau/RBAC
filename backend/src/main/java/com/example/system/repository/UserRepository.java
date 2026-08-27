package com.example.system.repository;

import com.example.system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("""
            select u from User u
            where lower(u.username) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(u.nickname, '')) like lower(concat('%', :keyword, '%'))
            """)
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
