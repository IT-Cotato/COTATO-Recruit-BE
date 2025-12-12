package org.cotato.backend.recruit.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Value("${springdoc.server-url:http://localhost:8080}")
	private String serverUrl;

	@Value("${springdoc.server-description:Local Server}")
	private String serverDescription;

	@Bean
	public OpenAPI openAPI() {
		String jwtSchemeName = "JWT Token";

		SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

		Components components =
				new Components()
						.addSecuritySchemes(
								jwtSchemeName,
								new SecurityScheme()
										.name(jwtSchemeName)
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT"));

		return new OpenAPI()
				.info(apiInfo())
				.servers(List.of(new Server().url(serverUrl).description(serverDescription)))
				.addSecurityItem(securityRequirement)
				.components(components);
	}

	private Info apiInfo() {
		return new Info()
				.title("COTATO Recruit API")
				.description("COTATO 모집 시스템 API 문서")
				.version("v1.0.0");
	}
}
