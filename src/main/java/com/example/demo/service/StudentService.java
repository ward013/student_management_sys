package com.example.demo.service;

import com.example.demo.entity.Student;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentService {
    private final List<Student> studentList = new ArrayList<>();
    public StudentService() {
        studentList.add(new Student(1,"章三",12,"12345"));
        studentList.add(new Student(2,"李四",13,"12345"));
        studentList.add(new Student(3,"王五",14,"12345"));
    }
    public List<Student> findAll(){
        return studentList;
    }
    public Student findById(int id){
        for(Student student : studentList){
            if(student.getId()==id){
                return student;
            }
        }return null;
    }
    public boolean addStudent(Student student){
        Student oldStudent = findById(student.getId());
        if(oldStudent != null){
            return false;
        }
        studentList.add(student);
        return true;
    }
    public boolean updateStudent(Integer id,Student newStudent){
        Student oldStudent = findById(id);
        if(oldStudent == null){
            return false;
        }
        oldStudent.setName(newStudent.getName());
        oldStudent.setAge(newStudent.getAge());
        oldStudent.setPhone(newStudent.getPhone());
        return true;
    }
    public boolean deleteStudent(Integer id){
        Student oldStudent = findById(id);
        if(oldStudent == null){
            return false;
        }
        studentList.remove(oldStudent);//用的是List列表的remove方法
        return true;
    }
}
