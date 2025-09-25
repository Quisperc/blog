package cn.civer.blog.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity(name = "t_user")
@Data
public class User {

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 用户名
    @Column(nullable = false)
    private String username;
    // 密码
    @Column(nullable = false)
    private String password;
    // 身份
    @Column(nullable = false)
    private role viewer;
    // 注册时间
    @Column(nullable = false)
    private LocalDateTime registerTime;
    // 登录时间
    @Column(nullable = false)
    private LocalDateTime loginTime;
}
enum role{
    manager,subscriber,viewer
}
