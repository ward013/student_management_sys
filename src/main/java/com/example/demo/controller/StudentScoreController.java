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

@RestController
@Validated
@RequestMapping(value = "/scores", produces = "application/json;charset=UTF-8")
public class StudentScoreController {
    private final StudentScoreService studentScoreService;
    private final AuthService authService;

    public StudentScoreController(StudentScoreService studentScoreService, AuthService authService) {
        this.studentScoreService = studentScoreService;
        this.authService = authService;
    }

    @GetMapping
    public Result<List<StudentScore>> findAll(@RequestParam(required = false) Integer studentId,
                                              @RequestParam(required = false) String courseName,
                                              @RequestParam(required = false) String semester,
                                              HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireAdminOrTeacher(currentUser);
        return Result.success(studentScoreService.findByConditions(studentId, courseName, semester));
    }

    @GetMapping("/{id}")
    public Result<StudentScore> findById(@PathVariable @Min(value = 1, message = "成绩记录id必须大于0") Long id,
                                         HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        StudentScore studentScore = studentScoreService.findById(id);
        authService.requireScoreReadable(currentUser, studentScore.getStudentId());
        return Result.success(studentScore);
    }

    @GetMapping("/me")
    public Result<List<StudentScore>> findMyScores(HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireBoundStudent(currentUser);
        return Result.success(studentScoreService.findByStudentId(currentUser.getIdentityId()));
    }

    @PostMapping
    public Result<StudentScore> addScore(@Valid @RequestBody StudentScore studentScore, HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireScoreWritable(currentUser);
        return Result.success("新增成绩成功", studentScoreService.addScore(studentScore, currentUser));
    }

    @PutMapping("/{id}")
    public Result<StudentScore> updateScore(@PathVariable @Min(value = 1, message = "成绩记录id必须大于0") Long id,
                                            @Valid @RequestBody StudentScore studentScore,
                                            HttpSession session) {
        UserAccount currentUser = authService.getCurrentUser(session);
        authService.requireScoreWritable(currentUser);
        return Result.success("修改成绩成功", studentScoreService.updateScore(id, studentScore, currentUser));
    }
}
