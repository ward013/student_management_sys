package com.example.demo.entity;

// Student 是学生实体类，用来承载数据库 student 表的一行数据。
// 字段名建议和数据库列名保持一致，这样 MyBatis 可以自动完成映射。
public class Student {
    private Integer id;
    private String name;
    private Integer age;
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
        // 这里做了简单校验，避免创建没有 id 的学生对象。
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
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
        // name 是学生的核心信息，这里不允许为空。
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
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
