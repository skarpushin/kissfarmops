package org.kissfarm.controller.mvc.controllers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Nice doc:
 * 
 * http://www.baeldung.com/swagger-2-documentation-for-spring-rest-api
 * http://heidloff.net/article/usage-of-swagger-2-0-in-spring-boot-applications-to-document-apis/
 * 
 * Open for API info: /swagger-ui.html
 * 
 * @author sergeyk
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Bean
	public Docket api() {
		// SEE: http://springfox.github.io/springfox/docs/snapshot/
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select().apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any()).build().genericModelSubstitutes(ResponseEntity.class)
				.genericModelSubstitutes(DeferredResult.class);
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("KISS Farm Controller REST API")
				.licenseUrl("https://github.com/IBM-Bluemix/news-aggregator/blob/master/LICENSE").version("1").build();
	}
}