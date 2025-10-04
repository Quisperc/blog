package cn.civer.blog.Config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
 
        servers = {
                @Server(description = "开发环境服务器", url = "http://localhost:8099"),
                @Server(description = "测试环境服务器", url = "https://test.civer.cn")
        },
        externalDocs = @ExternalDocumentation(
                description = "项目编译部署说明",
                url = "http://localhost:8099/deplay/readme.md"
        )
)
 
@Configuration
public class SpringDocConfig {
 
    @Bean
    public OpenAPI openAPI() {
        // 定义 JWT 的安全方案
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)      // 认证类型：HTTP
                .scheme("bearer")                   // 认证方案：bearer
                .bearerFormat("JWT")                // 格式：JWT
                .in(SecurityScheme.In.HEADER)       // 放在请求头
                .name("Authorization");             // Header 名称

        return new OpenAPI()
                // 接口文档基本信息
                .info(this.getApiInfo())
                // 注册安全方案
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth))
                // 设置全局安全需求（所有接口默认都要带上 token）
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    private Info getApiInfo() {
        return new Info()
                 // 配置文档标题
                .title("个人博客管理系统")
                // 配置文档描述
                .description("个人博客管理系统Api接口文档")
                // 配置作者信息
                .contact(new Contact().name("Quisper").url("https://blog.civer.cn").email("support@civer.cn"))
                // 配置License许可证信息
                .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                //
                .summary("个人博客管理系统示例文档")
                .termsOfService("https://www.civer.cn")
                // 配置版本号
                .version("1.0");
    }
}