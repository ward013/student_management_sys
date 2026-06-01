package com.example.demo.mapper;

import com.example.demo.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TeacherMapper {
    List<Teacher> findAll();

    Teacher findById(Integer id);
}
