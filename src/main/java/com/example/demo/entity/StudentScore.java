package com.example.demo.entity;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// StudentScore 对应 student_score 表，保存学生某门课程在某学期的成绩。
public class StudentScore {
    private Long id;

    // studentId 对应 student 表中的主键 id。
    @NotNull(message = "学生工号不能为空")
    @Min(value = 1, message = "学生工号必须大于0")
    private Integer studentId;

    // 课程名称，例如“高等数学”“大学英语”。
    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过100个字符")
    private String courseName;

    // 分数范围限制在 0 到 100 之间。
    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0.0", message = "成绩不能小于0")
    @DecimalMax(value = "100.0", message = "成绩不能大于100")
    private BigDecimal score;

    // 学期字段用于区分不同学期的同一门课程成绩。
    @NotBlank(message = "学期不能为空")
    @Size(max = 30, message = "学期长度不能超过30个字符")
    private String semester;

    // 这两个字段记录成绩的录入或维护教师信息。
    private Integer teacherId;

    @Size(max = 50, message = "老师姓名长度不能超过50个字符")
    private String teacherName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}
