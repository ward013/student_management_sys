package com.example.demo.mapper;

import com.example.demo.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// TeacherMapper 负责 teacher 表的查询。
@Mapper
public interface TeacherMapper {
    // 查询全部老师。
    List<Teacher> findAll();
    int insertTeacher(Teacher teacher);//返回类型用int，调用时直接判读是否>=0可以判断添加是否成功
    // 根据工号查询单个老师。
    Teacher findById(Integer id);
    // 更新教师信息
    int updateTeacher(Teacher teacher);
}
