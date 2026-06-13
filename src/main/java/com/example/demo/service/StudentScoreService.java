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

// 成绩服务：封装成绩查询、成绩维护，以及成绩记录和学生/老师身份的校验逻辑。
@Service
public class StudentScoreService {
    private final StudentScoreMapper studentScoreMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;

    // 构造方法注入成绩、学生、老师三个 Mapper。
    public StudentScoreService(StudentScoreMapper studentScoreMapper,
                               StudentMapper studentMapper,
                               TeacherMapper teacherMapper) {
        this.studentScoreMapper = studentScoreMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
    }

    // 按条件查询成绩列表。管理员/教师页会用到这个方法。
    public List<StudentScore> findByConditions(Integer studentId, String courseName, String semester) {
        return studentScoreMapper.findByConditions(studentId, courseName, semester);
    }

    // 根据成绩记录 id 查询单条成绩，查不到则抛业务异常。
    public StudentScore findById(Long id) {
        StudentScore studentScore = studentScoreMapper.findById(id);
        if (studentScore == null) {
            throw new BusinessException(404, "成绩记录不存在");
        }
        return studentScore;
    }

    // 根据学生工号查询该学生全部成绩。学生“我的成绩”页面会用到。
    public List<StudentScore> findByStudentId(Integer studentId) {
        ensureStudentExists(studentId);
        return studentScoreMapper.findByConditions(studentId, null, null);
    }

    // 新增成绩。写入前先确认学生存在，并在教师操作时补全教师信息。
    public StudentScore addScore(StudentScore studentScore, UserAccount currentUser) {
        ensureStudentExists(studentScore.getStudentId());
        applyTeacherInfo(studentScore, currentUser);
        int rows = studentScoreMapper.insertScore(studentScore);
        if (rows <= 0 || studentScore.getId() == null) {
            throw new BusinessException(500, "新增成绩失败");
        }
        return findById(studentScore.getId());
    }

    // 修改成绩。管理员保留原有教师信息，教师操作时改为当前教师身份信息。
    public StudentScore updateScore(Long id, StudentScore studentScore, UserAccount currentUser) {
        StudentScore existingScore = findById(id);
        ensureStudentExists(studentScore.getStudentId());
        studentScore.setId(id);
        // 管理员只改成绩内容时，不强制覆盖原先记录里的任课老师字段。
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

    // 校验学生工号是否存在于 student 表中。
    private void ensureStudentExists(Integer studentId) {
        if (studentMapper.findById(studentId) == null) {
            throw new BusinessException(404, "学生不存在");
        }
    }

    // 如果当前操作人是教师，则把当前教师身份写入成绩记录，确保数据来源清晰。
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
