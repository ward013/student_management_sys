package com.example.demo.service;

import com.example.demo.entity.StudentScore;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.UserAccount;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.StudentMapper;
import com.example.demo.mapper.StudentScoreMapper;
import com.example.demo.mapper.TeacherMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class StudentScoreService {
    private final StudentScoreMapper studentScoreMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;

    public StudentScoreService(StudentScoreMapper studentScoreMapper,
                               StudentMapper studentMapper,
                               TeacherMapper teacherMapper) {
        this.studentScoreMapper = studentScoreMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
    }

    public List<StudentScore> findByConditions(Integer studentId, String courseName, String semester) {
        return studentScoreMapper.findByConditions(studentId, courseName, semester);
    }

    public StudentScore findById(Long id) {
        StudentScore studentScore = studentScoreMapper.findById(id);
        if (studentScore == null) {
            throw new BusinessException(404, "成绩记录不存在");
        }
        return studentScore;
    }

    public List<StudentScore> findByStudentId(Integer studentId) {
        ensureStudentExists(studentId);
        return studentScoreMapper.findByConditions(studentId, null, null);
    }

    public StudentScore addScore(StudentScore studentScore, UserAccount currentUser) {
        ensureStudentExists(studentScore.getStudentId());
        applyTeacherInfo(studentScore, currentUser);
        int rows = studentScoreMapper.insertScore(studentScore);
        if (rows <= 0 || studentScore.getId() == null) {
            throw new BusinessException(500, "新增成绩失败");
        }
        return findById(studentScore.getId());
    }

    public StudentScore updateScore(Long id, StudentScore studentScore, UserAccount currentUser) {
        StudentScore existingScore = findById(id);
        ensureStudentExists(studentScore.getStudentId());
        studentScore.setId(id);
        if (currentUser.isAdmin()) {
            if (studentScore.getTeacherId() == null) {
                studentScore.setTeacherId(existingScore.getTeacherId());
            }
            if (!StringUtils.hasText(studentScore.getTeacherName())) {
                studentScore.setTeacherName(existingScore.getTeacherName());
            }
        }
        applyTeacherInfo(studentScore, currentUser);
        int rows = studentScoreMapper.updateScore(studentScore);
        if (rows <= 0) {
            throw new BusinessException(500, "修改成绩失败");
        }
        return findById(id);
    }

    private void ensureStudentExists(Integer studentId) {
        if (studentMapper.findById(studentId) == null) {
            throw new BusinessException(404, "学生不存在");
        }
    }

    private void applyTeacherInfo(StudentScore studentScore, UserAccount currentUser) {
        if (!"TEACHER".equalsIgnoreCase(currentUser.getIdentityType()) || currentUser.getIdentityId() == null) {
            return;
        }
        Teacher teacher = teacherMapper.findById(currentUser.getIdentityId());
        if (teacher == null) {
            throw new BusinessException(404, "老师身份不存在，无法维护成绩");
        }
        studentScore.setTeacherId(teacher.getId());
        studentScore.setTeacherName(teacher.getName());
        if (!StringUtils.hasText(studentScore.getSemester())) {
            throw new BusinessException(400, "学期不能为空");
        }
    }
}
