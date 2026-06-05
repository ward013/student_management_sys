package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Student;
import com.example.demo.entity.UserAccount;
import com.example.demo.service.AuthService;
import com.example.demo.service.StudentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController 表示这是一个接口控制器，方法返回值会直接写入 HTTP 响应体。
@RestController
@Validated
// 给本控制器下的所有接口统一加上 /students 前缀，并声明 JSON 响应使用 UTF-8，避免中文乱码。
@RequestMapping(value = "/students", produces = "application/json;charset=UTF-8")
public class StudentController {
    // Controller 不直接操作数据库，而是调用 Service 完成业务逻辑。
    // 包含两个service，即studentService和authService
    private final StudentService studentService;
    private final AuthService authService;

    // 构造方法注入：Spring 会自动把 StudentService 对象传进来。
    public StudentController(StudentService studentService, AuthService authService) {
        this.studentService = studentService;
        this.authService = authService;
    }

    // GET /students：查询所有学生。
    @GetMapping
    public Result<List<Student>> findAll(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdminOrTeacher(currentUser);
        return Result.success(studentService.findAll());
    }

    // GET /students/{id}：根据 id 查询单个学生。
    @GetMapping("/{id}")
    public Result<Student> findById(@PathVariable @Min(value = 1, message = "学生id必须大于0") Integer id,
                                    HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireStudentReadable(currentUser, id);
        return Result.success(studentService.findById(id));
    }

    @GetMapping("/me")
    public Result<Student> findMyStudentInfo(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        if (currentUser.isAdmin()) {
            throw new com.example.demo.exception.BusinessException(403, "管理员请使用学生列表接口");
        }
        authService.requireBoundStudent(currentUser);
        return Result.success(studentService.findById(currentUser.getIdentityId()));
    }

    // POST /students：新增学生。
    // @RequestBody 会把请求体中的 JSON 转换成 Student 对象。
    @PostMapping
    public Result<Student> addStudent(@Valid @RequestBody Student student, HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success("添加学生成功", studentService.addStudent(student));
    }

    // PUT /students/{id}：根据 id 修改学生信息。
    @PutMapping("/{id}")
    public Result<Student> updateStudent(@PathVariable @Min(value = 1, message = "学生id必须大于0") Integer id,
                                         @Valid @RequestBody Student student,
                                         HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success("修改学生成功", studentService.updateStudent(id, student));
    }

    // DELETE /students/{id}：根据 id 删除学生。
    @DeleteMapping("/{id}")
    public Result<Integer> deleteStudent(@PathVariable @Min(value = 1, message = "学生id必须大于0") Integer id,
                                         HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdmin(currentUser);
        return Result.success("删除学生成功", studentService.deleteStudent(id));
    }
}
