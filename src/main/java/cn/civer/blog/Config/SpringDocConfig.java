package cn.civer.blog.Config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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
        return new OpenAPI()
                // 配置接口文档基本信息
                .info(this.getApiInfo());
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