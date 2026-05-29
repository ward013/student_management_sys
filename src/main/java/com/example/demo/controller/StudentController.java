package com.example.demo.controller;

import com.example.demo.entity.Student;
import com.example.demo.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")// 路由
public class StudentController {
    private final StudentService studentService;
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }
    @GetMapping
    public List<Student> findALL() {
        return studentService.findAll();//调用service中的方法，即service给这里提供了接口调用

    }
    @GetMapping("/{id}")
    public Student findById(@PathVariable Integer id) {//通过@PathVariable声明传递{id}对应值
        return studentService.findById(id);
    }
    @PostMapping
    public String addStudent(@RequestBody Student student) {
        boolean result = studentService.addStudent(student);
        if (result) {
            return "adding student success";
        }else  {
            return "adding student fail";
        }
    }
    @PutMapping("/{id}")
    public String updateStudent(@PathVariable Integer id, @RequestBody Student student) {
        boolean result = studentService.updateStudent(id, student);
        if (result) {
            return "update student success";
        }else  {
            return "update student fail";
        }
    }
    @DeleteMapping("/{id}")
    public String deleteStudent(@PathVariable Integer id) {
        boolean result = studentService.deleteStudent(id);
        if (result) {
            return "delete student success";
        }else   {
            return "delete student fail";
        }
    }
}
