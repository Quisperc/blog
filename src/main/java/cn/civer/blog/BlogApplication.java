package cn.civer.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// 异步
@EnableAsync
// 定时任务
@EnableScheduling
@ServletComponentScan
public class BlogApplication {

    public static void main(String[] args) {

        SpringApplication.run(BlogApplication.class, args);
    }

}
