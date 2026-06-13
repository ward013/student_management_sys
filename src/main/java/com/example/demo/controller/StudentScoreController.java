package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.StudentScore;
import com.example.demo.entity.UserAccount;
import com.example.demo.service.AuthService;
import com.example.demo.service.StudentScoreService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 成绩控制器：负责成绩查询、查看本人成绩，以及管理员/教师维护成绩。
@RestController
@Validated
// 给本控制器下的所有接口统一加上 /scores 前缀，并声明 JSON 响应使用 UTF-8。
@RequestMapping(value = "/scores", produces = "application/json;charset=UTF-8")
public class StudentScoreController {
    private final StudentScoreService studentScoreService;
    private final AuthService authService;

    // 构造方法注入成绩服务和认证服务。
    public StudentScoreController(StudentScoreService studentScoreService, AuthService authService) {
        this.studentScoreService = studentScoreService;
        this.authService = authService;
    }

    // GET /scores：管理员和教师查询成绩列表，可按学生、课程、学期筛选。
    @GetMapping
    public Result<List<StudentScore>> findAll(@RequestParam(required = false) Integer studentId,
                                              @RequestParam(required = false) String courseName,
                                              @RequestParam(required = false) String semester,
                                              HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdminOrTeacher(currentUser);
        return Result.success(studentScoreService.findByConditions(studentId, courseName, semester));
    }

    // GET /scores/{id}：根据成绩记录 id 查询单条成绩详情。
    @GetMapping("/{id}")
    public Result<StudentScore> findById(@PathVariable @Min(value = 1, message = "成绩记录id必须大于0") Long id,
                                         HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        StudentScore studentScore = studentScoreService.findById(id);
        authService.requireScoreReadable(currentUser, studentScore.getStudentId());
        return Result.success(studentScore);
    }

    // GET /scores/me：学生查看自己绑定工号下的全部成绩。
    @GetMapping("/me")
    public Result<List<StudentScore>> findMyScores(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireBoundStudent(currentUser);
        return Result.success(studentScoreService.findByStudentId(currentUser.getIdentityId()));
    }

    // POST /scores：管理员或教师新增成绩记录。
    @PostMapping
    public Result<StudentScore> addScore(@Valid @RequestBody StudentScore studentScore, HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireScoreWritable(currentUser);
        return Result.success("新增成绩成功", studentScoreService.addScore(studentScore, currentUser));
    }

    // PUT /scores/{id}：管理员或教师修改一条已有成绩记录。
    @PutMapping("/{id}")
    public Result<StudentScore> updateScore(@PathVariable @Min(value = 1, message = "成绩记录id必须大于0") Long id,
                                            @Valid @RequestBody StudentScore studentScore,
                                            HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireScoreWritable(currentUser);
        return Result.success("修改成绩成功", studentScoreService.updateScore(id, studentScore, currentUser));
    }
}
