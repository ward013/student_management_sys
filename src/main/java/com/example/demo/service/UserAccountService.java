package com.example.demo.service;

import com.example.demo.common.PasswordUtils;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.entity.UserAccount;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.UserAccountMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserAccountService {
    private final UserAccountMapper userAccountMapper;
    private final AuthService authService;

    public UserAccountService(UserAccountMapper userAccountMapper, AuthService authService) {
        this.userAccountMapper = userAccountMapper;
        this.authService = authService;
    }

    public List<UserAccount> findAll() {
        return userAccountMapper.findAll();
    }

    public UserAccount updateUser(Integer id, UserUpdateRequest request) {
        UserAccount existingUser = userAccountMapper.findById(id);
        if (existingUser == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (StringUtils.hasText(request.getUsername())) {
            UserAccount userByUsername = userAccountMapper.findByUsername(request.getUsername().trim());
            if (userByUsername != null && !userByUsername.getId().equals(id)) {
                throw new BusinessException(409, "用户名已存在");
            }
            existingUser.setUsername(request.getUsername().trim());
        }

        if (StringUtils.hasText(request.getPassword())) {
            existingUser.setPasswordHash(PasswordUtils.hash(request.getPassword()));
        }

        existingUser.setRole(authService.normalizeRole(request.getRole()));

        String identityType = authService.normalizeIdentityType(request.getIdentityType());
        if ("NONE".equals(identityType)) {
            existingUser.setIdentityType("NONE");
            existingUser.setIdentityId(null);
        } else {
            if (request.getIdentityId() == null || request.getIdentityId() <= 0) {
                throw new BusinessException(400, "绑定工号必须大于0");
            }
            authService.validateIdentityExists(identityType, request.getIdentityId());
            UserAccount boundUser = userAccountMapper.findByIdentityBinding(identityType, request.getIdentityId());
            if (boundUser != null && !boundUser.getId().equals(existingUser.getId())) {
                throw new BusinessException(409, "该身份信息已被其他账号绑定");
            }
            existingUser.setIdentityType(identityType);
            existingUser.setIdentityId(request.getIdentityId());
        }

        userAccountMapper.updateUser(existingUser);
        return userAccountMapper.findById(id);
    }
}
