package com.example.demo.service;

import com.example.demo.entity.Student;
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

    // 根据 id 查询一个学生；如果数据库中没有对应记录，Mapper 通常会返回 null。
    public Student findById(int id) {
        return studentMapper.findById(id);
    }

    // 新增学生。MyBatis 的 insert/update/delete 通常返回受影响的行数。
    public boolean addStudent(Student student) {
        int rows = studentMapper.insertStudent(student);
        return rows > 0;
    }

    // 修改学生。路径里的 id 更可信，所以先把 id 设置到请求体对象里。
    public boolean updateStudent(Integer id, Student student) {
        student.setId(id);
        int rows = studentMapper.updateStudent(student);
        return rows > 0;
    }

    // 删除学生。返回 true 表示数据库中确实删除了一条记录。
    public boolean deleteStudent(Integer id) {
        int rows = studentMapper.deleteStudent(id);
        return rows > 0;
    }
}
