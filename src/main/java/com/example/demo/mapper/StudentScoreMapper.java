package com.example.demo.mapper;

import com.example.demo.entity.StudentScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudentScoreMapper {
    List<StudentScore> findByConditions(@Param("studentId") Integer studentId,
                                        @Param("courseName") String courseName,
                                        @Param("semester") String semester);

    StudentScore findById(Long id);

    int insertScore(StudentScore studentScore);

    int updateScore(StudentScore studentScore);
}
