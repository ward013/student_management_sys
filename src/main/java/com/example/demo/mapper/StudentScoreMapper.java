package com.example.demo.mapper;

import com.example.demo.entity.StudentScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// StudentScoreMapper 负责 student_score 表的查询和写入。
@Mapper
public interface StudentScoreMapper {
    // 按学生、课程、学期筛选成绩列表；不传的条件会被忽略。
    List<StudentScore> findByConditions(@Param("studentId") Integer studentId,
                                        @Param("courseName") String courseName,
                                        @Param("semester") String semester);

    // 根据成绩记录 id 查询单条成绩。
    StudentScore findById(Long id);

    // 新增成绩记录。
    int insertScore(StudentScore studentScore);

    // 更新成绩记录。
    int updateScore(StudentScore studentScore);
}
