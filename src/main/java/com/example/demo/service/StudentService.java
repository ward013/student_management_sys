package com.example.demo.service;

import com.example.demo.entity.Student;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.StudentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

// @Service 表示这是业务层组件，Spring 会自动创建并管理它。
@Service
public class StudentService {
    // Service 通过 Mapper 访问数据库，避免 Controller 直接写 SQL。
    private final StudentMapper studentMapper;

    // 构造方法注入 Mapper。这样依赖关系更清楚，也方便后续测试。
    public StudentService(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    // 查询所有学生，直接把 Mapper 查询到的结果返回给 Controller。
    public List<Student> findAll() {
        return studentMapper.findAll();
    }

    // 根据 id 查询一个学生；如果查不到，则抛出业务异常。
    public Student findById(int id) {
        Student student = studentMapper.findById(id);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }
        return student;
    }

    // 新增学生。如果 id 已存在，则抛出业务异常。
    public Student addStudent(Student student) {
        Student existingStudent = studentMapper.findById(student.getId());
        if (existingStudent != null) {
            throw new BusinessException(409, "添加学生失败，id 已存在");
        }
        int rows = studentMapper.insertStudent(student);
        if (rows <= 0) {
            throw new BusinessException(500, "添加学生失败");
        }
        return student;
    }

    // 修改学生。路径里的 id 更可信，所以先把 id 设置到请求体对象里。
    public Student updateStudent(Integer id, Student student) {
        findById(id);
        student.setId(id);
        int rows = studentMapper.updateStudent(student);
        if (rows <= 0) {
            throw new BusinessException(500, "修改学生失败");
        }
        return student;
    }

    // 删除学生。删除前先检查是否存在。
    public Integer deleteStudent(Integer id) {
        findById(id);
        int rows = studentMapper.deleteStudent(id);
        if (rows <= 0) {
            throw new BusinessException(500, "删除学生失败");
        }
        return id;
    }
}
