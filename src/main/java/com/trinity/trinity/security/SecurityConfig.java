package com.trinity.trinity.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static final String ADMIN = "ROLE_ADMIN";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**").permitAll()

                        // Somente o administrador
                        .requestMatchers("/usuarios/**", "/h2-console/**").hasAuthority(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/membros/desativar/**").hasAuthority(ADMIN)

                        // Membros
                        .requestMatchers("/membros/**").hasAnyAuthority(ADMIN, "GERENCIAR_MEMBROS")

                        // Avisos e eventos: todos visualizam, só quem tem permissão gerencia
                        .requestMatchers(HttpMethod.GET, "/avisos", "/eventos").authenticated()
                        .requestMatchers("/avisos/**").hasAnyAuthority(ADMIN, "GERENCIAR_AVISOS")
                        .requestMatchers("/eventos/**").hasAnyAuthority(ADMIN, "GERENCIAR_EVENTOS")

                        // Escalas: telas de cadastro antes das de visualização
                        .requestMatchers("/escalas/nova", "/escalas/editar/**", "/funcoes-musicais/**")
                        .hasAnyAuthority(ADMIN, "GERENCIAR_ESCALAS")
                        .requestMatchers(HttpMethod.GET, "/escalas", "/escalas/*")
                        .hasAnyAuthority(ADMIN, "VISUALIZAR_ESCALAS", "GERENCIAR_ESCALAS")
                        .requestMatchers("/escalas/**").hasAnyAuthority(ADMIN, "GERENCIAR_ESCALAS")

                        // Patrimônio e financeiro
                        .requestMatchers("/patrimonios/**").hasAnyAuthority(ADMIN, "GERENCIAR_PATRIMONIO")
                        .requestMatchers("/financeiro/**").hasAnyAuthority(ADMIN, "GERENCIAR_FINANCEIRO")

                        .anyRequest().authenticated()
                )

                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }


}