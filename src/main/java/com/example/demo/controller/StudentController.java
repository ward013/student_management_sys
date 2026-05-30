package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Student;
import com.example.demo.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController 表示这是一个接口控制器，方法返回值会直接写入 HTTP 响应体。
@RestController
// 给本控制器下的所有接口统一加上 /students 前缀，并声明 JSON 响应使用 UTF-8，避免中文乱码。
@RequestMapping(value = "/students", produces = "application/json;charset=UTF-8")
public class StudentController {
    // Controller 不直接操作数据库，而是调用 Service 完成业务逻辑。
    private final StudentService studentService;

    // 构造方法注入：Spring 会自动把 StudentService 对象传进来。
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // GET /students：查询所有学生。
    @GetMapping
    public Result<List<Student>> findAll() {
        return Result.success(studentService.findAll());
    }

    // GET /students/{id}：根据 id 查询单个学生。
    @GetMapping("/{id}")
    public Result<Student> findById(@PathVariable Integer id) {
        // @PathVariable 会把路径中的 {id} 取出来，赋值给方法参数 id。
        Student student = studentService.findById(id);
        if (student == null) {
            return Result.fail("学生不存在");
        }
        return Result.success(student);
    }

    // POST /students：新增学生。
    // @RequestBody 会把请求体中的 JSON 转换成 Student 对象。
    @PostMapping
    public Result<Student> addStudent(@RequestBody Student student) {
        boolean result = studentService.addStudent(student);
        if (result) {
            return Result.success("添加学生成功", student);
        } else {
            return Result.fail("添加学生失败，id 可能已经存在");
        }
    }

    // PUT /students/{id}：根据 id 修改学生信息。
    @PutMapping("/{id}")
    public Result<Student> updateStudent(@PathVariable Integer id, @RequestBody Student student) {
        boolean result = studentService.updateStudent(id, student);
        if (result) {
            return Result.success("修改学生成功", student);
        } else {
            return Result.fail("修改学生失败，学生不存在");
        }
    }

    // DELETE /students/{id}：根据 id 删除学生。
    @DeleteMapping("/{id}")
    public Result<Integer> deleteStudent(@PathVariable Integer id) {
        boolean result = studentService.deleteStudent(id);
        if (result) {
            return Result.success("删除学生成功", id);
        } else {
            return Result.fail("删除学生失败，学生不存在");
        }
    }
}
