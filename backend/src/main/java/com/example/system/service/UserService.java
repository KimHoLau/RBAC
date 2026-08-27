package com.example.system.service;

import com.example.system.common.PageResult;
import com.example.system.dto.AuthInfoVO;
import com.example.system.dto.UserDTO;
import com.example.system.dto.UserUpsertRequest;

import java.util.List;

public interface UserService {

    PageResult<UserDTO> page(String keyword, int page, int size);

    UserDTO getUserInfoById(Long id);

    AuthInfoVO getAuthInfo(Long id);

    void create(UserUpsertRequest request);

    void update(Long id, UserUpsertRequest request);

    void delete(Long id);

    void resetPassword(Long id);

    void assignRoles(Long id, List<Long> roleIds);
}
