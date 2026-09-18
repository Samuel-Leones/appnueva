package com.accesorios.gestion.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConfiguracionOpenApi {

    @Bean
    fun openApiPersonalizado(): OpenAPI {
        val nombreEsquemaBearer = "bearerAuth"

        return OpenAPI()
            .info(
                Info()
                    .title("API — Sistema de Gestión de Inventario y Ventas")
                    .version("1.0.0")
                    .description("API REST para la gestión de inventario, ventas, clientes y reportes de una tienda de accesorios de moto")
            )
            .addSecurityItem(SecurityRequirement().addList(nombreEsquemaBearer))
            .components(
                Components().addSecuritySchemes(
                    nombreEsquemaBearer,
                    SecurityScheme()
                        .name(nombreEsquemaBearer)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
    }
}
