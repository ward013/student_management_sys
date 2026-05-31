package com.example.demo.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Student 是学生实体类，用来承载数据库 student 表的一行数据。
// 字段名建议和数据库列名保持一致，这样 MyBatis 可以自动完成映射。
public class Student {
    @NotNull(message = "学生id不能为空")
    @Min(value = 1, message = "学生id必须大于0")
    private Integer id;

    @NotBlank(message = "学生姓名不能为空")
    @Size(max = 50, message = "学生姓名长度不能超过50个字符")
    private String name;

    @NotNull(message = "学生年龄不能为空")
    @Min(value = 0, message = "学生年龄不能小于0")
    private Integer age;

    @NotBlank(message = "学生手机号不能为空")
    @Size(max = 20, message = "学生手机号长度不能超过20个字符")
    private String phone;

    // 无参构造方法很重要：Spring/Jackson/MyBatis 创建对象时经常需要它。
    public Student() {
    }

    // 带参构造方法方便在代码里快速创建 Student 对象。
    public Student(Integer id, String name, Integer age, String phone) {
        setId(id);
        setName(name);
        setAge(age);
        setPhone(phone);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        // toString 主要用于调试时打印对象内容。
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", phone='" + phone + '\'' +
                '}';
    }
}
