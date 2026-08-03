package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import com.jayway.jsonpath.JsonPath;
import io.github.ramiroabadie.backend.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lo último que a HU-20 le faltaba del backend: la subida de afiches (D72/D77/D88). Los tests
 * que importan más que los del camino feliz son los tres del final: **el contador que no se
 * reinicia** y los **dos solapamientos**, porque son las promesas del contrato que el código
 * puede romper sin que se note hasta que un afiche viejo aparece en un lugar nuevo.
 *
 * <p>Vive en el paquete interno del módulo a propósito: los dos tests de solapamiento tienen que
 * llamar a las piezas de a una para forzar el entrelazado, y esas piezas no son públicas ni
 * tienen por qué serlo. Por HTTP no se puede forzar el orden: la ventana real dura lo que tarda
 * un {@code update} y un test así pasaría por suerte.</p>
 *
 * <p>El directorio es propio de esta clase y se vacía antes de empezar. Eso le cuesta un contexto
 * de Spring aparte —y su contenedor—, y es el precio de no mirar los archivos de otra corrida.</p>
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "afiches.directorio=target/afiches-de-prueba")
class AfichesTest {

	private static final String TOKEN_CSRF = "un-token-de-prueba";

	private static final Path DEPOSITO = Path.of("target", "afiches-de-prueba");

	/** Cuánto se queda abierta la transacción que gana, para que la otra tenga con qué chocar. */
	private static final Duration SOLAPE = Duration.ofMillis(500);

	@Autowired
	private MockMvc mockMvc;

	/** Las tres piezas de la subida, para los tests que necesitan forzar el orden. */
	@Autowired
	private AficheService afiches;

	@Autowired
	private VersionesDeAfiche versiones;

	@Autowired
	private AlmacenDeAfiches almacen;

	@Autowired
	private ProcesadorDeAfiche procesador;

	/** Los dos que hacen falta para forzar un solapamiento real: la fila y la transacción. */
	@Autowired
	private ProduccionRepository repositorio;

	@Autowired
	private TransactionTemplate transacciones;

	@BeforeAll
	static void vaciarElDeposito() throws IOException {
		if (Files.exists(DEPOSITO)) {
			try (var contenido = Files.walk(DEPOSITO)) {
				contenido.sorted(Comparator.reverseOrder()).forEach(archivo -> archivo.toFile().delete());
			}
		}
	}

	/**
	 * El camino feliz entero: la imagen se convierte a JPEG, se encaja en la caja de D79 sin
	 * recortar ni deformar, y la URL aparece en la ficha —que es donde el frontend la busca— con
	 * la versión adentro del nombre.
	 */
	@Test
	void subirElAficheLoGuardaEncajadoYLoPublicaEnLaFicha() throws Exception {
		Long obra = crearProduccion("La obra con afiche");

		subir(obra, png(2400, 3200), "afiche.png")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.aficheUrl").value("/afiches/" + obra + "-1.jpg"));

		mockMvc.perform(get("/api/producciones/" + obra))
				.andExpect(jsonPath("$.aficheUrl").value("/afiches/" + obra + "-1.jpg"));
		mockMvc.perform(get("/api/en-cartel"))
				.andExpect(jsonPath("$.enCartel[?(@.id == " + obra + ")].aficheUrl")
						.value("/afiches/" + obra + "-1.jpg"));

		BufferedImage guardado = leer(archivo(obra, 1));
		assertThat(guardado.getWidth()).isEqualTo(1200);
		assertThat(guardado.getHeight()).isEqualTo(1600);
		assertThat(formatoDe(archivo(obra, 1))).isEqualTo("JPEG");
	}

	/** Un afiche más chico que la caja se guarda como vino: agrandarlo es peso sin información. */
	@Test
	void unAficheChicoNoSeAgranda() throws Exception {
		Long obra = crearProduccion("La obra del afiche chico");

		subir(obra, jpeg(300, 400), "afiche.jpg").andExpect(status().isOk());

		BufferedImage guardado = leer(archivo(obra, 1));
		assertThat(guardado.getWidth()).isEqualTo(300);
		assertThat(guardado.getHeight()).isEqualTo(400);
	}

	/**
	 * El reemplazo, que es el motivo entero del versionado (D77): la URL nueva es otra URL, y el
	 * archivo viejo se borra recién después de que la base apunte al nuevo.
	 */
	@Test
	void reemplazarEstrenaUrlYSeLlevaElArchivoViejo() throws Exception {
		Long obra = crearProduccion("La obra del afiche reemplazado");

		subir(obra, png(600, 800), "primero.png").andExpect(status().isOk());
		subir(obra, png(600, 800), "segundo.png")
				.andExpect(jsonPath("$.aficheUrl").value("/afiches/" + obra + "-2.jpg"));

		assertThat(archivo(obra, 2)).exists();
		assertThat(archivo(obra, 1)).doesNotExist();
	}

	/**
	 * ⚠️ La promesa central del contrato: {@code afiche_version} **solo sube y nunca se
	 * reinicia**, aunque la ficha se quede sin afiche en el medio. Si al borrar se reiniciara,
	 * el próximo afiche volvería a la URL 1 — que ya está cacheada por un año en máquinas que no
	 * controlamos, con otra imagen adentro, y ahí el {@code immutable} pasa de garantía a mentira.
	 *
	 * <p>De paso, las dos mitades de la idempotencia: borrar dos veces es 204, y la ficha sin
	 * afiche es un estado normal y no un error (D71).</p>
	 */
	@Test
	void borrarNoReiniciaElContadorYEsIdempotente() throws Exception {
		Long obra = crearProduccion("La obra del afiche que va y viene");
		subir(obra, png(600, 800), "primero.png").andExpect(status().isOk());

		borrar(obra).andExpect(status().isNoContent());
		borrar(obra).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/producciones/" + obra))
				.andExpect(jsonPath("$.aficheUrl").isEmpty());
		assertThat(archivo(obra, 1)).doesNotExist();

		subir(obra, png(600, 800), "tercero.png")
				.andExpect(jsonPath("$.aficheUrl").value("/afiches/" + obra + "-2.jpg"));
	}

	/** El 404 queda para lo que sí es un error de verdad: que esa producción no exista. */
	@Test
	void unaFichaQueNoExisteEs404EnLosDosSentidos() throws Exception {
		subir(999999L, png(600, 800), "afiche.png").andExpect(status().isNotFound());
		borrar(999999L).andExpect(status().isNotFound());
	}

	/**
	 * Lo que no es una imagen no gasta un número de versión: se valida antes de reservar, así que
	 * la ficha queda como estaba y el contador también.
	 */
	@Test
	void loQueNoEsUnaImagenNoEntraYNoGastaVersion() throws Exception {
		Long obra = crearProduccion("La obra del archivo que no era");

		subir(obra, "esto es un texto, no un afiche".getBytes(StandardCharsets.UTF_8), "cosas.txt")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").isNotEmpty());
		subir(obra, new byte[0], "vacio.png")
				.andExpect(status().isBadRequest());

		subir(obra, png(600, 800), "ahora-si.png")
				.andExpect(jsonPath("$.aficheUrl").value("/afiches/" + obra + "-1.jpg"));
	}

	/**
	 * El tope que sí acota la memoria (D88). El archivo de este test pesa menos de 100 bytes y
	 * declara 400 millones de píxeles: si el código decodificara antes de mirar las dimensiones,
	 * este test no fallaría, se quedaría sin heap.
	 */
	@Test
	void unaImagenConDimensionesEnormesSeRechazaSinDecodificarla() throws Exception {
		Long obra = crearProduccion("La obra de la bomba de descompresión");

		subir(obra, pngSoloCabecera(20000, 20000), "bomba.png")
				.andExpect(status().isBadRequest());
	}

	/**
	 * ⚠️ <b>La orientación EXIF se lee en los tres formatos y no sólo en JPEG</b>, porque los tres
	 * pueden traerla: el PNG en su bloque {@code eXIf} y el WebP en el suyo. Ninguno de los tres
	 * lectores la aplica solo, así que si no se aplicara acá, el afiche quedaría acostado en la
	 * ficha. Los tres casos, con los lados intercambiados como prueba.
	 */
	@Test
	void laOrientacionExifSeAplicaEnLosTresFormatos() throws Exception {
		Long conJpeg = crearProduccion("La foto acostada en JPEG");
		Long conPng = crearProduccion("La foto acostada en PNG");
		Long conWebp = crearProduccion("La foto acostada en WebP");

		subir(conJpeg, jpegConOrientacion(40, 20, 6), "acostada.jpg").andExpect(status().isOk());
		subir(conPng, pngConOrientacion(40, 20, 6), "acostada.png").andExpect(status().isOk());
		subir(conWebp, recurso("afiche-orientado.webp"), "acostada.webp").andExpect(status().isOk());

		for (Long obra : List.of(conJpeg, conPng, conWebp)) {
			BufferedImage guardado = leer(archivo(obra, 1));
			assertThat(guardado.getWidth()).as("ancho de la ficha %d", obra).isEqualTo(20);
			assertThat(guardado.getHeight()).as("alto de la ficha %d", obra).isEqualTo(40);
		}
	}

	/**
	 * Las dos entradas que justifican la dependencia de D88, con archivos de verdad y no con
	 * confianza en el {@code pom.xml}: un <b>WebP</b>, que el JDK directamente no sabe leer —y hoy
	 * cualquier imagen bajada de una web moderna llega así—, y un <b>JPEG CMYK</b>, que es lo que
	 * sale de imprenta y con lo que el lector del JDK se planta. Los dos salen del otro lado como
	 * un JPEG RGB.
	 */
	@Test
	void elWebpYElJpegDeImprentaEntranComoCualquierAfiche() throws Exception {
		Long conWebp = crearProduccion("La obra del afiche en WebP");
		Long conCmyk = crearProduccion("La obra del afiche de imprenta");

		subir(conWebp, recurso("afiche.webp"), "afiche.webp")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.aficheUrl").value("/afiches/" + conWebp + "-1.jpg"));
		subir(conCmyk, recurso("afiche-cmyk.jpg"), "afiche.jpg")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.aficheUrl").value("/afiches/" + conCmyk + "-1.jpg"));

		for (Long obra : List.of(conWebp, conCmyk)) {
			assertThat(formatoDe(archivo(obra, 1))).isEqualTo("JPEG");
			BufferedImage guardado = leer(archivo(obra, 1));
			assertThat(guardado.getWidth()).isEqualTo(400);
			assertThat(guardado.getHeight()).isEqualTo(600);
		}
	}

	/** El afiche es parte del catálogo: lo toca el admin y nadie más (D7). */
	@Test
	void elAficheSoloLoTocaElAdmin() throws Exception {
		Long obra = crearProduccion("La obra del afiche ajeno");

		mockMvc.perform(conCsrf(multipart("/api/admin/producciones/" + obra + "/afiche")
				.file(new MockMultipartFile("archivo", "afiche.png", MediaType.IMAGE_PNG_VALUE, png(600, 800)))))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(conCsrf(multipart("/api/admin/producciones/" + obra + "/afiche"))
				.file(new MockMultipartFile("archivo", "afiche.png", MediaType.IMAGE_PNG_VALUE, png(600, 800)))
				.with(user("comun").roles("USUARIO")))
				.andExpect(status().isForbidden());
		mockMvc.perform(conCsrf(delete("/api/admin/producciones/" + obra + "/afiche"))
				.with(user("comun").roles("USUARIO")))
				.andExpect(status().isForbidden());
	}

	/**
	 * Dos subidas intercaladas: las dos reservan, las dos escriben, y publican **al revés** del
	 * orden en que reservaron. Lo que prueba es que el resultado no dependa del orden de reserva
	 * —gana la última en publicar y la otra se va con su archivo—, que es la regla que ordena
	 * todo el contrato: <b>la base nunca apunta a un archivo que no está</b>.
	 *
	 * <p>⚠️ <b>Lo que este test NO prueba, y conviene tenerlo escrito porque es fácil creer que
	 * sí:</b> el bloqueo de la fila. Acá los pasos corren uno después del otro en el mismo hilo,
	 * así que sacarle el {@code @Lock} al repositorio lo dejaría igual de verde — comprobado.
	 * Eso lo prueban los dos de más abajo, que sí solapan dos transacciones.</p>
	 */
	@Test
	void dosPublicacionesIntercaladasDejanLaFichaApuntandoAUnArchivoQueExiste() throws Exception {
		Long obra = crearProduccion("La obra de las dos subidas a la vez");
		byte[] jpeg = procesador.aJpeg(png(600, 800));

		int primera = versiones.reservar(obra);
		int segunda = versiones.reservar(obra);
		assertThat(segunda).isEqualTo(primera + 1);
		almacen.escribir(obra, primera, jpeg);
		almacen.escribir(obra, segunda, jpeg);

		publicarYLimpiar(obra, segunda);
		publicarYLimpiar(obra, primera);

		mockMvc.perform(get("/api/producciones/" + obra))
				.andExpect(jsonPath("$.aficheUrl").value("/afiches/" + obra + "-" + primera + ".jpg"));
		assertThat(archivo(obra, primera)).exists();
		assertThat(archivo(obra, segunda)).doesNotExist();
	}

	/**
	 * Una subida en curso con un borrado en el medio, también intercalado en un solo hilo: el
	 * borrado gana el estado —la ficha se queda sin afiche— y la subida que venía atrás publica
	 * después sin llevarse puesto nada. Misma advertencia que el anterior: prueba el orden, no el
	 * bloqueo.
	 */
	@Test
	void unaSubidaIntercaladaConUnBorradoNoDejaLaFichaRota() throws Exception {
		Long obra = crearProduccion("La obra del borrado en el medio");
		subir(obra, png(600, 800), "primero.png").andExpect(status().isOk());
		byte[] jpeg = procesador.aJpeg(png(600, 800));

		int enCurso = versiones.reservar(obra);
		almacen.escribir(obra, enCurso, jpeg);

		borrar(obra).andExpect(status().isNoContent());
		assertThat(archivo(obra, 1)).doesNotExist();

		assertThat(versiones.publicar(obra, enCurso)).isNull();

		mockMvc.perform(get("/api/producciones/" + obra))
				.andExpect(jsonPath("$.aficheUrl").value("/afiches/" + obra + "-" + enCurso + ".jpg"));
		assertThat(archivo(obra, enCurso)).exists();
	}

	/**
	 * Y lo mismo de verdad concurrente, con las dos subidas enteras largando a la vez: cuál gana
	 * lo decide el planificador y no importa, porque lo que se afirma vale para los dos órdenes
	 * posibles — queda **un** archivo, es el que la ficha apunta, y las dos versiones fueron
	 * distintas—. Sin el bloqueo de la fila al publicar, las dos capturarían "no había nada
	 * antes", ninguna borraría, y quedarían dos archivos: por eso la cuenta es la afirmación.
	 */
	@Test
	void dosSubidasEnterasALaVezDejanUnSoloArchivo() throws Exception {
		Long obra = crearProduccion("La obra del doble clic");
		byte[] imagen = png(600, 800);
		CountDownLatch largada = new CountDownLatch(1);
		ExecutorService hilos = Executors.newFixedThreadPool(2);
		try {
			Future<?> unaSubida = hilos.submit(() -> subirEnParalelo(largada, obra, imagen));
			Future<?> laOtra = hilos.submit(() -> subirEnParalelo(largada, obra, imagen));
			largada.countDown();
			unaSubida.get();
			laOtra.get();
		}
		finally {
			hilos.shutdownNow();
		}

		MvcResult ficha = mockMvc.perform(get("/api/producciones/" + obra)).andReturn();
		String url = JsonPath.read(ficha.getResponse().getContentAsString(), "$.aficheUrl");
		assertThat(url).isIn("/afiches/" + obra + "-1.jpg", "/afiches/" + obra + "-2.jpg");
		assertThat(archivos(obra)).containsExactly(DEPOSITO.resolve(url.substring("/afiches/".length())));
	}

	/**
	 * ⚠️ <b>El test que le da sentido al {@code select ... for update} de D77</b>, y el que faltaba:
	 * dos publicaciones que <b>se solapan de verdad</b>, con el entrelazado forzado y no librado
	 * al planificador. Un hilo toma la fila y se queda adentro de la transacción medio segundo;
	 * el otro intenta publicar en el medio.
	 *
	 * <p>Lo que se afirma es lo único que distingue haber bloqueado de no haberlo hecho: <b>el
	 * segundo tiene que esperar y ver publicada la versión del primero</b>. Sin el bloqueo, su
	 * lectura pasa de largo, devuelve "no había ninguna" y el archivo del primero queda huérfano
	 * mientras la ficha apunta a otro lado. <b>Comprobado en los dos sentidos</b>: sacándole el
	 * {@code @Lock} al repositorio, este test falla.</p>
	 */
	@Test
	void publicarConLaFilaTomadaEsperaYVeLoQueElOtroPublico() throws Exception {
		Long obra = crearProduccion("La obra de las dos transacciones");
		byte[] jpeg = procesador.aJpeg(png(600, 800));
		int primera = versiones.reservar(obra);
		int segunda = versiones.reservar(obra);
		almacen.escribir(obra, primera, jpeg);
		almacen.escribir(obra, segunda, jpeg);

		CountDownLatch filaTomada = new CountDownLatch(1);
		ExecutorService hilos = Executors.newFixedThreadPool(2);
		try {
			Future<?> laQueBloquea = hilos.submit(() -> transacciones.executeWithoutResult(estado -> {
				repositorio.bloquearPorId(obra).orElseThrow().publicarAfiche(primera);
				filaTomada.countDown();
				dormir(SOLAPE);
			}));
			Future<Integer> laQueEspera = hilos.submit(() -> {
				filaTomada.await();
				return versiones.publicar(obra, segunda);
			});

			laQueBloquea.get();
			assertThat(laQueEspera.get())
					.as("la segunda publicación tiene que ver la versión que la primera publicó")
					.isEqualTo(primera);
		}
		finally {
			hilos.shutdownNow();
		}
	}

	/**
	 * El otro solapamiento que D77 pide con sincronización explícita: una subida y un borrado
	 * peleando por la misma fila. El borrado entra primero y se queda adentro de la transacción;
	 * la subida que venía atrás tiene que esperarlo y encontrarse con que ya no hay afiche
	 * publicado — <b>{@code null} y no la versión vieja</b>, que es lo que devolvería sin el
	 * bloqueo y la llevaría a borrar un archivo que ya no le corresponde. Comprobado también en
	 * los dos sentidos.
	 */
	@Test
	void publicarMientrasOtroBorraEsperaYSeEncuentraSinAfiche() throws Exception {
		Long obra = crearProduccion("La obra del borrado y la subida a la vez");
		subir(obra, png(600, 800), "primero.png").andExpect(status().isOk());
		byte[] jpeg = procesador.aJpeg(png(600, 800));
		int enCurso = versiones.reservar(obra);
		almacen.escribir(obra, enCurso, jpeg);

		CountDownLatch filaTomada = new CountDownLatch(1);
		ExecutorService hilos = Executors.newFixedThreadPool(2);
		try {
			Future<?> elBorrado = hilos.submit(() -> transacciones.executeWithoutResult(estado -> {
				repositorio.bloquearPorId(obra).orElseThrow().despublicarAfiche();
				filaTomada.countDown();
				dormir(SOLAPE);
			}));
			Future<Integer> laSubida = hilos.submit(() -> {
				filaTomada.await();
				return versiones.publicar(obra, enCurso);
			});

			elBorrado.get();
			assertThat(laSubida.get())
					.as("la subida que esperó tiene que ver que el borrado dejó la ficha sin afiche")
					.isNull();
		}
		finally {
			hilos.shutdownNow();
		}
	}

	private static void dormir(Duration cuanto) {
		try {
			Thread.sleep(cuanto.toMillis());
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(ex);
		}
	}

	private Object subirEnParalelo(CountDownLatch largada, Long obra, byte[] imagen) {
		try {
			largada.await();
			afiches.subir(obra, imagen);
			return null;
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(ex);
		}
	}

	private void publicarYLimpiar(Long obra, int version) {
		Integer anterior = versiones.publicar(obra, version);
		if (anterior != null) {
			almacen.borrar(obra, anterior);
		}
	}

	private ResultActions subir(Long produccionId, byte[] contenido, String nombre) throws Exception {
		return mockMvc.perform(conCsrf(multipart("/api/admin/producciones/" + produccionId + "/afiche"))
				.file(new MockMultipartFile("archivo", nombre, MediaType.IMAGE_PNG_VALUE, contenido))
				.with(user("jefa").roles("ADMIN")));
	}

	private ResultActions borrar(Long produccionId) throws Exception {
		return mockMvc.perform(conCsrf(delete("/api/admin/producciones/" + produccionId + "/afiche"))
				.with(user("jefa").roles("ADMIN")));
	}

	private Long crearProduccion(String titulo) throws Exception {
		MvcResult resultado = mockMvc.perform(conCsrf(post("/api/admin/producciones"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"titulo":"%s","estado":"EN_CARTEL"}""".formatted(titulo))
				.with(user("jefa").roles("ADMIN")))
				.andExpect(status().isCreated())
				.andReturn();
		return ((Number) JsonPath.read(resultado.getResponse().getContentAsString(), "$.id")).longValue();
	}

	private static Path archivo(Long produccionId, int version) {
		return DEPOSITO.resolve(produccionId + "-" + version + ".jpg");
	}

	/** Los archivos de esa producción, que es lo que hay que contar cuando dos subidas compiten. */
	private static List<Path> archivos(Long produccionId) throws IOException {
		try (var contenido = Files.list(DEPOSITO)) {
			return contenido.filter(archivo -> archivo.getFileName().toString().startsWith(produccionId + "-"))
					.sorted()
					.toList();
		}
	}

	private static BufferedImage leer(Path archivo) throws IOException {
		return ImageIO.read(archivo.toFile());
	}

	private static String formatoDe(Path archivo) throws IOException {
		try (var entrada = ImageIO.createImageInputStream(archivo.toFile())) {
			var lectores = ImageIO.getImageReaders(entrada);
			return lectores.hasNext() ? lectores.next().getFormatName() : null;
		}
	}

	private static byte[] png(int ancho, int alto) throws IOException {
		return imagen(ancho, alto, "png");
	}

	private static byte[] jpeg(int ancho, int alto) throws IOException {
		return imagen(ancho, alto, "jpeg");
	}

	/** Un degradé, no un color plano: un color plano sobrevive a cualquier escala y no prueba nada. */
	private static byte[] imagen(int ancho, int alto, String formato) throws IOException {
		BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		Graphics2D pincel = imagen.createGraphics();
		for (int x = 0; x < ancho; x++) {
			pincel.setColor(new Color(x % 256, (x * 2) % 256, 128));
			pincel.drawLine(x, 0, x, alto);
		}
		pincel.dispose();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ImageIO.write(imagen, formato, bytes);
		return bytes.toByteArray();
	}

	/**
	 * Un JPEG con un EXIF mínimo escrito a mano: el segmento APP1 va enseguida del SOI y lleva un
	 * TIFF con una sola entrada, la orientación. Se arma a mano porque el JDK no escribe EXIF y
	 * la alternativa sería versionar una foto de celular adentro del repo.
	 */
	private static byte[] jpegConOrientacion(int ancho, int alto, int orientacion) throws IOException {
		byte[] original = jpeg(ancho, alto);
		byte[] tiff = tiffConOrientacion(orientacion);
		byte[] identificador = { 'E', 'x', 'i', 'f', 0, 0 };
		int largo = 2 + identificador.length + tiff.length;
		ByteArrayOutputStream salida = new ByteArrayOutputStream();
		salida.write(original, 0, 2); // SOI
		salida.write(0xFF);
		salida.write(0xE1); // APP1
		salida.write(largo >> 8);
		salida.write(largo & 0xFF);
		salida.write(identificador);
		salida.write(tiff);
		salida.write(original, 2, original.length - 2);
		return salida.toByteArray();
	}

	/** Los tres archivos que no se pueden fabricar desde Java: ver el README de esa carpeta. */
	private static byte[] recurso(String nombre) throws IOException {
		try (InputStream entrada = AfichesTest.class.getResourceAsStream("/afiches/" + nombre)) {
			if (entrada == null) {
				throw new IllegalStateException("Falta el archivo de prueba /afiches/" + nombre);
			}
			return entrada.readAllBytes();
		}
	}

	/**
	 * Un PNG con un bloque {@code eXIf} metido a mano enseguida de la cabecera, que es donde la
	 * especificación lo pide. Mismo TIFF que el del JPEG, pero sin el prefijo {@code Exif\0\0}:
	 * el PNG guarda el bloque pelado, y esa diferencia entre contenedores es justamente lo que el
	 * código tiene que tolerar.
	 */
	private static byte[] pngConOrientacion(int ancho, int alto, int orientacion) throws IOException {
		byte[] original = png(ancho, alto);
		byte[] tiff = tiffConOrientacion(orientacion);
		ByteArrayOutputStream nombreYDatos = new ByteArrayOutputStream();
		nombreYDatos.write(new byte[] { 'e', 'X', 'I', 'f' });
		nombreYDatos.write(tiff);
		CRC32 control = new CRC32();
		control.update(nombreYDatos.toByteArray());

		int finDeLaCabecera = 8 + 25; // firma + el bloque IHDR entero
		ByteArrayOutputStream salida = new ByteArrayOutputStream();
		salida.write(original, 0, finDeLaCabecera);
		salida.write(enteroDe(tiff.length));
		salida.write(nombreYDatos.toByteArray());
		salida.write(enteroDe((int) control.getValue()));
		salida.write(original, finDeLaCabecera, original.length - finDeLaCabecera);
		return salida.toByteArray();
	}

	/** Un TIFF mínimo con una sola entrada: la orientación. */
	private static byte[] tiffConOrientacion(int orientacion) {
		return new byte[] {
				'M', 'M', 0, 42, 0, 0, 0, 8,          // big endian, 42, IFD0 en el byte 8
				0, 1,                                  // una sola entrada
				1, 18, 0, 3, 0, 0, 0, 1,               // tag 0x0112 (orientación), tipo SHORT, cantidad 1
				0, (byte) orientacion, 0, 0,           // el valor, alineado a la izquierda
				0, 0, 0, 0                             // no hay IFD siguiente
		};
	}

	/**
	 * Un PNG que es solo firma y cabecera: dice cuánto mide y no trae un solo píxel. Es la bomba
	 * de descompresión en su forma más honesta — pesa nada y pide 400 millones de píxeles.
	 */
	private static byte[] pngSoloCabecera(int ancho, int alto) throws IOException {
		ByteArrayOutputStream cabecera = new ByteArrayOutputStream();
		cabecera.write(new byte[] { 'I', 'H', 'D', 'R' });
		cabecera.write(enteroDe(ancho));
		cabecera.write(enteroDe(alto));
		cabecera.write(new byte[] { 8, 2, 0, 0, 0 }); // 8 bits, color verdadero, sin entrelazado
		byte[] datos = cabecera.toByteArray();
		CRC32 control = new CRC32();
		control.update(datos);

		ByteArrayOutputStream salida = new ByteArrayOutputStream();
		salida.write(new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A });
		salida.write(enteroDe(datos.length - 4)); // el largo no cuenta el nombre del bloque
		salida.write(datos);
		salida.write(enteroDe((int) control.getValue()));
		return salida.toByteArray();
	}

	private static byte[] enteroDe(int valor) {
		return new byte[] { (byte) (valor >>> 24), (byte) (valor >>> 16), (byte) (valor >>> 8), (byte) valor };
	}

	private MockMultipartHttpServletRequestBuilder conCsrf(MockMultipartHttpServletRequestBuilder peticion) {
		peticion.cookie(new Cookie("XSRF-TOKEN", TOKEN_CSRF));
		peticion.header("X-XSRF-TOKEN", TOKEN_CSRF);
		return peticion;
	}

	private MockHttpServletRequestBuilder conCsrf(MockHttpServletRequestBuilder peticion) {
		return peticion.cookie(new Cookie("XSRF-TOKEN", TOKEN_CSRF)).header("X-XSRF-TOKEN", TOKEN_CSRF);
	}
}
