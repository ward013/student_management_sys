package com.example.demo.service;

import com.example.demo.entity.Teacher;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.TeacherMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {
    private final TeacherMapper teacherMapper;

    public TeacherService(TeacherMapper teacherMapper) {
        this.teacherMapper = teacherMapper;
    }

    public List<Teacher> findAll() {
        return teacherMapper.findAll();
    }

    public Teacher findById(Integer id) {
        Teacher teacher = teacherMapper.findById(id);
        if (teacher == null) {
            throw new BusinessException(404, "老师不存在");
        }
        return teacher;
    }
}
