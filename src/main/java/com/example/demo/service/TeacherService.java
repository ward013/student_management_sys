package com.example.demo.service;

import com.example.demo.entity.Teacher;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.TeacherMapper;
import org.springframework.stereotype.Service;

import java.util.List;

// TeacherService 封装老师相关的业务逻辑。
@Service
public class TeacherService {
    private final TeacherMapper teacherMapper;

    public TeacherService(TeacherMapper teacherMapper) {
        this.teacherMapper = teacherMapper;
    }

    // 查询全部老师。
    public List<Teacher> findAll() {
        return teacherMapper.findAll();
    }

    // 添加老师信息
    public Teacher addTeacher(Teacher teacher) {
        Teacher existingTeacher = teacherMapper.findById(teacher.getId());
        if (existingTeacher != null) {
            throw new BusinessException(409,"添加教师信息失败！");
        }
        int row=teacherMapper.insertTeacher(teacher);
        if(row<=0){
            throw new BusinessException(500,"添加教师信息失败！");
        }
        return teacher;
    }
    // 根据工号查询老师，查不到则抛业务异常。
    public Teacher findById(Integer id) {
        Teacher teacher = teacherMapper.findById(id);
        if (teacher == null) {
            throw new BusinessException(404, "老师不存在");
        }
        return teacher;
    }
    //
    public Teacher updateTeacher(int id,Teacher teacher) {
        findById(id);
        teacher.setId(id);
        int rows = teacherMapper.updateTeacher(teacher);
        if (rows <= 0) {
            throw new BusinessException(500,"修改教师信息失败");
        }
        return teacher;
    }
}
