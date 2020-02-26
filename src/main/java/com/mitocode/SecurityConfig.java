package com.mitocode;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.WebSessionServerCsrfTokenRepository;

import reactor.core.publisher.Mono;


@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder (
    ) {
	
	return new BCryptPasswordEncoder();
	
    }
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain (
	    ServerHttpSecurity http
    ) {
	
	http.cors().and().authorizeExchange().pathMatchers( "/", "/css/**", "/js/**", "/img/**" ).permitAll()
		.pathMatchers( "/login" ).permitAll().pathMatchers( "/logout" ).authenticated()
		.pathMatchers(
			"/platos/**" )
		.hasAuthority( "ADMIN" ).pathMatchers( "/facturas/**" )
		.access( (mono, context) -> mono
			.map( auth -> auth.getAuthorities().stream()
				.filter( e -> e.getAuthority().equalsIgnoreCase( "ADMIN" ) ).count() > 0 )
			.map( AuthorizationDecision::new ) )
		.pathMatchers( "/clientes/**" )
		.access( (mono,
			context) -> mono
				.map( auth -> auth.getAuthorities().stream()
					.filter( e -> e.getAuthority().equals( "ADMIN" )
						|| e.getAuthority().equals( "USER" ) )
					.count() > 0 )
				.map( AuthorizationDecision::new ) )
		.anyExchange().authenticated()
		.and()
		.formLogin().loginPage( "/login" )
		.and().logout().logoutUrl( "/logout" ).and()
		.csrf(csrf -> csrf.tokenFromMultipartDataEnabled( true ));
	
	return http
	// .and()
	// .exceptionHandling()
	// .accessDeniedHandler(new
	// HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN))
	.exceptionHandling() // .accessDeniedPage("/403") webflux no presente
	.accessDeniedHandler(
		(exchange, exception) -> Mono.error( new AccessDeniedException( "No estas autorizado" ) ) )
	.and().build();	
                
	
    }
    
}
