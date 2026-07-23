package io.github.ramiroabadie.backend.aplicacion.internal.seguridad;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.savedrequest.NullRequestCache;

/**
 * La autorización es un concern transversal de la capa de aplicación, no de los módulos
 * (MODULE_MAP.md): el candado de {@code /api/admin/**} se define acá y por eso Catálogo sigue
 * sin depender de Identidad. Nada de este paquete importa clases de otro módulo — solo tipos de
 * Spring — así que {@code ModulithArchitectureTest} sigue verde.
 *
 * <p>Reentrada de D52: Spring Security vuelve al proyecto con HU-01/02. Auth por sesión con
 * cookie HTTP-only (D44); nada de JWT hasta que exista la app móvil.</p>
 */
@Configuration
class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(UserDetailsService usuarios, PasswordEncoder encoder) {
		DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(usuarios);
		proveedor.setPasswordEncoder(encoder);
		return new ProviderManager(proveedor);
	}

	/**
	 * Token CSRF en cookie legible por JavaScript: el frontend de la Fase 4 lo lee de
	 * {@code XSRF-TOKEN} y lo devuelve en el header {@code X-XSRF-TOKEN}. No se enmascara
	 * (BREACH) porque el token nunca se renderiza en el cuerpo de una respuesta: esta API
	 * no dibuja formularios.
	 */
	@Bean
	CsrfTokenRepository csrfTokenRepository() {
		CookieCsrfTokenRepository repositorio = CookieCsrfTokenRepository.withHttpOnlyFalse();
		repositorio.setCookieCustomizer(cookie -> cookie.sameSite("Lax"));
		return repositorio;
	}

	@Bean
	SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	/**
	 * Sin carga diferida: así el token se genera y la cookie viaja en cualquier respuesta, y el
	 * cliente siempre tiene uno a mano antes del primer POST. Con el manejador por defecto, el
	 * token se crea recién cuando alguien lo lee — y en la respuesta del login no lo lee nadie,
	 * con lo cual la primera escritura después de entrar se comería un 403.
	 */
	@Bean
	CsrfTokenRequestHandler csrfTokenRequestHandler() {
		CsrfTokenRequestAttributeHandler manejador = new CsrfTokenRequestAttributeHandler();
		manejador.setCsrfRequestAttributeName(null);
		return manejador;
	}

	/**
	 * Lo que hace el filtro de login estándar al autenticar, para que el login JSON de
	 * {@code AuthController} no se lo pierda: nuevo id de sesión (fijación de sesión) y token
	 * CSRF nuevo, porque el viejo lo vio quien todavía no estaba logueado.
	 */
	@Bean
	SessionAuthenticationStrategy sessionAuthenticationStrategy(CsrfTokenRepository csrfTokenRepository,
			CsrfTokenRequestHandler csrfTokenRequestHandler) {
		CsrfAuthenticationStrategy csrf = new CsrfAuthenticationStrategy(csrfTokenRepository);
		csrf.setRequestHandler(csrfTokenRequestHandler);
		return new CompositeSessionAuthenticationStrategy(List.of(
				new ChangeSessionIdAuthenticationStrategy(), csrf));
	}

	@Bean
	SecurityFilterChain filtros(HttpSecurity http, CsrfTokenRepository csrfTokenRepository,
			CsrfTokenRequestHandler csrfTokenRequestHandler,
			SecurityContextRepository securityContextRepository) throws Exception {
		return http
				.authorizeHttpRequests(reglas -> reglas
						.requestMatchers(HttpMethod.POST, "/api/auth/registro", "/api/auth/login").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/auth/yo").authenticated()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						// Todo el contenido es público y se lee sin cuenta (D21).
						.requestMatchers(HttpMethod.GET, "/api/**").permitAll()
						.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
						// Un 404 o un 400 se sirven con un segundo despacho del contenedor hacia
						// /error, que también pasa por acá: si /error pide login, todo error real
						// termina disfrazado de 401. El cuerpo que arma Boot no lleva mensaje ni
						// stacktrace, así que abrirlo no filtra nada.
						.requestMatchers("/error").permitAll()
						.anyRequest().authenticated())
				.csrf(csrf -> csrf
						.csrfTokenRepository(csrfTokenRepository)
						.csrfTokenRequestHandler(csrfTokenRequestHandler))
				.securityContext(contexto -> contexto.securityContextRepository(securityContextRepository))
				// Sin caché de peticiones: guardarla crea una sesión en cada 401 anónimo, y esta
				// API nunca va a redirigir a nadie de vuelta a lo que quiso hacer antes de entrar.
				.requestCache(cache -> cache.requestCache(new NullRequestCache()))
				.logout(salida -> salida
						.logoutUrl("/api/auth/logout")
						.deleteCookies("JSESSIONID")
						.logoutSuccessHandler((peticion, respuesta, autenticacion) -> {
							// El handler de CSRF borra el token al salir; sin uno nuevo, el próximo
							// login se comería un 403 por no tener qué mandar.
							csrfTokenRepository.saveToken(csrfTokenRepository.generateToken(peticion),
									peticion, respuesta);
							respuesta.setStatus(HttpStatus.NO_CONTENT.value());
						}))
				// Una API responde 401, no redirige a un formulario que no existe.
				.exceptionHandling(errores -> errores
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.build();
	}
}
