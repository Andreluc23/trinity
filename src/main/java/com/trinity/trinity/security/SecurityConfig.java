package com.trinity.trinity.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;

@Configuration
public class SecurityConfig {

    private static final String ADMIN = "ROLE_ADMIN";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   EmissorDeTokens emissorDeTokens,
                                                   UsuarioDetailsService usuarioDetailsService) throws Exception {

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

                .addFilterBefore(new JwtAutenticacaoFiltro(jwtService, emissorDeTokens, usuarioDetailsService),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new CsrfCookieFiltro(), CsrfFilter.class)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .csrf(csrf -> csrf
                        .csrfTokenRepository(new CookieCsrfTokenRepository())
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
                        .ignoringRequestMatchers("/h2-console/**")
                )

                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            emissorDeTokens.emitirNoLogin(authentication.getName(), request, response);
                            response.sendRedirect(request.getContextPath() + "/dashboard");
                        })
                        .permitAll()
                )

                .logout(logout -> logout
                        .addLogoutHandler((request, response, authentication) -> emissorDeTokens.revogar(request))
                        .deleteCookies(JwtCookie.TOKEN, JwtCookie.REFRESH)
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}