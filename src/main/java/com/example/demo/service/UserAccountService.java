package com.example.demo.service;

import com.example.demo.common.PasswordUtils;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.entity.UserAccount;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.UserAccountMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

// UserAccountService 处理账号管理业务，主要面向管理员使用。
@Service
public class UserAccountService {
    private final UserAccountMapper userAccountMapper;
    private final AuthService authService;

    public UserAccountService(UserAccountMapper userAccountMapper, AuthService authService) {
        this.userAccountMapper = userAccountMapper;
        this.authService = authService;
    }

    // 查询全部账号。
    public List<UserAccount> findAll() {
        return userAccountMapper.findAll();
    }

    // 更新用户资料、角色和绑定身份。
    public UserAccount updateUser(Integer id, UserUpdateRequest request) {
        UserAccount existingUser = userAccountMapper.findById(id);
        if (existingUser == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 用户名允许修改，但必须保持唯一。
        if (StringUtils.hasText(request.getUsername())) {
            UserAccount userByUsername = userAccountMapper.findByUsername(request.getUsername().trim());
            if (userByUsername != null && !userByUsername.getId().equals(id)) {
                throw new BusinessException(409, "用户名已存在");
            }
            existingUser.setUsername(request.getUsername().trim());
        }

        // 如果管理员填写了新密码，这里会重新摘要后保存。
        if (StringUtils.hasText(request.getPassword())) {
            existingUser.setPasswordHash(PasswordUtils.hash(request.getPassword()));
        }

        existingUser.setRole(authService.normalizeRole(request.getRole()));

        String identityType = authService.normalizeIdentityType(request.getIdentityType());
        if ("NONE".equals(identityType)) {
            // 管理员可以把账号恢复成未绑定状态。
            existingUser.setIdentityType("NONE");
            existingUser.setIdentityId(null);
        } else {
            // 绑定学生或老师时，需要校验目标记录存在且没有被别的账号占用。
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
