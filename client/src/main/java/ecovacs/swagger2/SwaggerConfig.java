package ecovacs.swagger2;

import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

/**
 * @Description swagger在线接口插件配置类
 * @Date 2019年10月17日
 * @author wuye
 */
@Configuration
@EnableSwagger2 //开启在线接口文档
public class SwaggerConfig {

    /**
     * @Description
     * @Date 2019年10月17日
     * @author wuye
     * @return
     */
    @Bean
    public Docket accessToken() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo( apiInfo())
               // .host("47.100.188.237:8082")//写了绑定，不写默认localhost:port
                .select()
                .apis(RequestHandlerSelectors.basePackage("ecovacs"))//配置扫描的控制器类包
                .paths(paths())//配置符合指定规则的路径接口才生成在线接口，用于定制那些接口在ui中展示
                .build()
                .enable(true);
    }
    @SuppressWarnings("unchecked")
    private Predicate<String> paths() {
        return or(regex("/*/*/.*?"));
    }
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("后台管理系统API")
                .description("接口地址:https://")
                .version("2.0")
                .contact(new Contact("wuye", "http://www.xxx.com/web", "wuye@ihep.ac.cn"))
                .build();
    }

}