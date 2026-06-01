package com.example.demo.mapper;

import com.example.demo.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserAccountMapper {
    List<UserAccount> findAll();

    UserAccount findById(Integer id);

    UserAccount findByUsername(String username);

    UserAccount findByIdentityBinding(@Param("identityType") String identityType,
                                      @Param("identityId") Integer identityId);

    int insertUser(UserAccount userAccount);

    int updateUser(UserAccount userAccount);
}
