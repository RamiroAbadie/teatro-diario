package io.github.ramiroabadie.backend;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los criterios de aceptación de HU-07, ejecutables. La clase vive en el paquete raíz y no
 * adentro de un módulo porque la historia es una sola y la cumplen dos: Catálogo busca
 * producciones y personas, Identidad busca usuarios (D23). Nadie compone nada.
 *
 * <p>Lo que estos tests defienden es la tolerancia: una búsqueda que solo encuentra el título
 * escrito perfecto es una búsqueda que no sirve para el gesto de las 23:30 (P8). Por eso las
 * pruebas que importan son las del typo y las del título a medio escribir, no la del acierto
 * exacto.</p>
 *
 * <p>La base es la misma para toda la clase y para las otras clases de test, así que los
 * títulos y los nombres son inventados y raros a propósito: cada test verifica que lo suyo
 * salga primero, no que sea lo único que sale.</p>
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class BusquedaTest {

	private static final String TOKEN_CSRF = "un-token-de-prueba";

	@Autowired
	private MockMvc mockMvc;

	/** HU-07: el título escrito bien encuentra la ficha, y la respuesta alcanza para elegirla. */
	@Test
	void elTituloExactoEncuentraLaProduccion() throws Exception {
		crearProduccion("Zorzal de las cornisas");

		mockMvc.perform(get("/api/buscar/producciones").param("q", "Zorzal de las cornisas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].titulo").value("Zorzal de las cornisas"))
				.andExpect(jsonPath("$[0].id").isNumber())
				.andExpect(jsonPath("$[0].estado").value("EN_CARTEL"));
	}

	/** El criterio de HU-07: tolerancia a typos. Sin esto, pg_trgm no estaría haciendo nada. */
	@Test
	void unTypoEncuentraLaProduccionIgual() throws Exception {
		crearProduccion("Cuarteto de la intemperie");

		mockMvc.perform(get("/api/buscar/producciones").param("q", "cuarteto de la intenperie"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].titulo").value("Cuarteto de la intemperie"));

		mockMvc.perform(get("/api/buscar/producciones").param("q", "cuarteto intemperie"))
				.andExpect(jsonPath("$[0].titulo").value("Cuarteto de la intemperie"));
	}

	/**
	 * El autocompletado del gesto de registro (HU-09), que es media palabra de un título largo.
	 * Es el caso que la similitud sobre el título entero no cubre: de ahí el {@code <%}.
	 */
	@Test
	void mediaPalabraAlcanzaParaElAutocompletado() throws Exception {
		crearProduccion("Vestigios de un domingo cualquiera en Villa Crespo");

		mockMvc.perform(get("/api/buscar/producciones").param("q", "vestig"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].titulo").value("Vestigios de un domingo cualquiera en Villa Crespo"));

		mockMvc.perform(get("/api/buscar/producciones").param("q", "villa crespo"))
				.andExpect(jsonPath("$[0].titulo").value("Vestigios de un domingo cualquiera en Villa Crespo"));
	}

	/** Entre dos parecidas gana la que más se parece: es todo el orden de relevancia que hay (D23). */
	@Test
	void elResultadoMasParecidoVaPrimero() throws Exception {
		crearProduccion("Nocturama");
		crearProduccion("Nocturama y otras vigilias del conurbano");

		mockMvc.perform(get("/api/buscar/producciones").param("q", "Nocturama"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].titulo").value("Nocturama"))
				.andExpect(jsonPath("$[1].titulo").value("Nocturama y otras vigilias del conurbano"));
	}

	/**
	 * Sin resultados no es un error: es la respuesta que el frontend convierte en el camino a
	 * sugerir la producción faltante (HU-08). La lista vacía es lo que esa pantalla necesita.
	 */
	@Test
	void loQueNoEstaEnElCatalogoDevuelveNada() throws Exception {
		mockMvc.perform(get("/api/buscar/producciones").param("q", "xilofonorquesta submarina"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	/**
	 * Un campo de búsqueda recién abierto no pide el catálogo entero. Y sin el parámetro es un
	 * pedido mal hecho, no una búsqueda vacía.
	 */
	@Test
	void laConsultaVaciaNoDevuelveElCatalogoEntero() throws Exception {
		crearProduccion("Cualquier cosa con tal de estar en la base");

		mockMvc.perform(get("/api/buscar/producciones").param("q", "   "))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());

		mockMvc.perform(get("/api/buscar/producciones"))
				.andExpect(status().isBadRequest());
	}

	/** HU-07 sobre el segundo tipo: personas, con la misma tolerancia. */
	@Test
	void lasPersonasSeBuscanConLaMismaTolerancia() throws Exception {
		crearPersona("Marilú Marini");

		mockMvc.perform(get("/api/buscar/personas").param("q", "marini"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Marilú Marini"));

		mockMvc.perform(get("/api/buscar/personas").param("q", "mariu marini"))
				.andExpect(jsonPath("$[0].nombre").value("Marilú Marini"));
	}

	/** HU-07 sobre el tercero: usuarios. El email no se busca, que es lo único que no es público. */
	@Test
	void losUsuariosSeBuscanPorUsernameYNoPorEmail() throws Exception {
		crearCuenta("zulemadelmonte", "girasol.violeta@example.com");

		mockMvc.perform(get("/api/buscar/usuarios").param("q", "zulema"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].username").value("zulemadelmonte"));

		mockMvc.perform(get("/api/buscar/usuarios").param("q", "girasol.violeta"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.username == 'zulemadelmonte')]").isEmpty());
	}

	/** Las tres se usan sin cuenta: buscar es leer, y leer no pide nada (D21). */
	@Test
	void buscarNoPideCuenta() throws Exception {
		mockMvc.perform(get("/api/buscar/producciones").param("q", "algo")).andExpect(status().isOk());
		mockMvc.perform(get("/api/buscar/personas").param("q", "algo")).andExpect(status().isOk());
		mockMvc.perform(get("/api/buscar/usuarios").param("q", "algo")).andExpect(status().isOk());
	}

	private Long crearProduccion(String titulo) throws Exception {
		MvcResult resultado = mockMvc.perform(json(post("/api/admin/producciones"), """
				{"titulo":"%s","estado":"EN_CARTEL"}""".formatted(titulo))
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isCreated())
				.andReturn();
		return id(resultado);
	}

	private Long crearPersona(String nombre) throws Exception {
		MvcResult resultado = mockMvc.perform(json(post("/api/admin/personas"), """
				{"nombre":"%s"}""".formatted(nombre))
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isCreated())
				.andReturn();
		return id(resultado);
	}

	private Long crearCuenta(String username, String email) throws Exception {
		MvcResult resultado = mockMvc.perform(json(post("/api/auth/registro"), """
				{"username":"%s","email":"%s","password":"unaClaveLarga"}"""
				.formatted(username, email)))
				.andExpect(status().isCreated())
				.andReturn();
		return id(resultado);
	}

	private static Long id(MvcResult resultado) throws Exception {
		return ((Number) JsonPath.read(resultado.getResponse().getContentAsString(), "$.id")).longValue();
	}

	private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String cuerpo) {
		return peticion.cookie(new Cookie("XSRF-TOKEN", TOKEN_CSRF)).header("X-XSRF-TOKEN", TOKEN_CSRF)
				.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
	}
}
