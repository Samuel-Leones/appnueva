package com.accesorios.gestion.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class ConfiguracionSeguridad(
    private val filtroAutenticacionJwt: FiltroAutenticacionJwt,
    private val servicioDetallesUsuario: ServicioDetallesUsuarioPersonalizado
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * Permite que el frontend (servido con Live Server u otro puerto distinto
     * al del backend) pueda consumir esta API desde el navegador.
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuracion = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("http://localhost:*", "http://127.0.0.1:*")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type")
            allowCredentials = true
        }

        val fuente = UrlBasedCorsConfigurationSource()
        fuente.registerCorsConfiguration("/**", configuracion)
        return fuente
    }

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(servicioDetallesUsuario)
        provider.setPasswordEncoder(passwordEncoder())
        return provider
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.POST, "/autenticacion/login").permitAll()
                it.requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()


                it.requestMatchers(HttpMethod.POST, "/autenticacion/registro").hasAnyRole("SUPER_USUARIO", "ADMINISTRADOR")


                it.requestMatchers(HttpMethod.GET, "/productos/**").hasAnyRole("EMPLEADO", "ADMINISTRADOR")
                it.requestMatchers(HttpMethod.POST, "/productos").hasAnyRole("EMPLEADO", "ADMINISTRADOR")
                it.requestMatchers(HttpMethod.PUT, "/productos/**").hasAnyRole("EMPLEADO", "ADMINISTRADOR")
                it.requestMatchers(HttpMethod.DELETE, "/productos/**").hasRole("ADMINISTRADOR")

                it.requestMatchers(HttpMethod.GET, "/categorias/**").hasAnyRole("EMPLEADO", "ADMINISTRADOR")
                it.requestMatchers(HttpMethod.POST, "/categorias").hasRole("ADMINISTRADOR")
                it.requestMatchers(HttpMethod.PUT, "/categorias/**").hasRole("ADMINISTRADOR")
                it.requestMatchers(HttpMethod.DELETE, "/categorias/**").hasRole("ADMINISTRADOR")

                it.requestMatchers(HttpMethod.GET, "/proveedores/**").hasAnyRole("EMPLEADO", "ADMINISTRADOR")
                it.requestMatchers(HttpMethod.POST, "/proveedores").hasRole("ADMINISTRADOR")
                it.requestMatchers(HttpMethod.PUT, "/proveedores/**").hasRole("ADMINISTRADOR")
                it.requestMatchers(HttpMethod.DELETE, "/proveedores/**").hasRole("ADMINISTRADOR")


                it.requestMatchers(HttpMethod.GET, "/clientes/**").hasAnyRole("EMPLEADO", "ADMINISTRADOR")
                it.requestMatchers(HttpMethod.POST, "/clientes").hasAnyRole("EMPLEADO", "ADMINISTRADOR")
                it.requestMatchers(HttpMethod.PUT, "/clientes/**").hasAnyRole("EMPLEADO", "ADMINISTRADOR")


                it.requestMatchers(HttpMethod.POST, "/inventario/movimientos").hasAnyRole("EMPLEADO", "ADMINISTRADOR")
                it.requestMatchers(HttpMethod.GET, "/inventario/**").hasAnyRole("EMPLEADO", "ADMINISTRADOR")


                it.requestMatchers(HttpMethod.POST, "/ventas/**").hasAnyRole("EMPLEADO", "ADMINISTRADOR")
                it.requestMatchers(HttpMethod.GET, "/ventas/**").hasAnyRole("EMPLEADO", "ADMINISTRADOR")


                it.requestMatchers("/reportes/**").hasRole("ADMINISTRADOR")
                it.requestMatchers("/panel/**").hasRole("ADMINISTRADOR")
                it.requestMatchers("/auditoria/**").hasRole("ADMINISTRADOR")

                it.anyRequest().authenticated()
            }
            .addFilterBefore(filtroAutenticacionJwt, UsernamePasswordAuthenticationFilter::class.java)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .build()
    }
}
