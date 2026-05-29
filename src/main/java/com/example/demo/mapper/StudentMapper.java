package com.example.demo.mapper;

import com.example.demo.entity.Student;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// @Mapper 告诉 MyBatis：这是一个 Mapper 接口，需要为它生成实现类。
// 方法名要和 resources/mapper/StudentMapper.xml 中 SQL 标签的 id 保持一致。
@Mapper
public interface StudentMapper {
    // 查询 student 表中的所有学生。
    List<Student> findAll();

    // 根据主键 id 查询一个学生。
    Student findById(Integer id);

    // 新增学生，返回受影响的行数。
    int insertStudent(Student student);

    // 修改学生，返回受影响的行数。
    int updateStudent(Student student);

    // 删除学生，返回受影响的行数。
    int deleteStudent(Integer id);
}
