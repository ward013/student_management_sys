package com.example.demo.mapper;

import com.example.demo.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// UserAccountMapper 负责 user_account 表的增删改查。
@Mapper
public interface UserAccountMapper {
    // 查询全部账号。
    List<UserAccount> findAll();

    // 根据主键查询账号。定义
    UserAccount findById(Integer id);//对应的xml里的 select id, username, password_hash, role, identity_type, identity_id
    //返回的是一个UserAccount对象

    // 根据用户名查询账号，登录和注册查重都会用到。
    UserAccount findByUsername(String username);

    // 根据绑定身份查询账号，防止一个工号被多个账号重复绑定。
    UserAccount findByIdentityBinding(@Param("identityType") String identityType,
                                      @Param("identityId") Integer identityId);

    // 新增账号。
    int insertUser(UserAccount userAccount);

    // 更新账号。
    int updateUser(UserAccount userAccount);
}
