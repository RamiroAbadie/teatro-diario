package io.github.ramiroabadie.backend.diario.internal.registro;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.ramiroabadie.backend.catalogo.CatalogoProducciones;
import io.github.ramiroabadie.backend.catalogo.ProduccionBasica;
import io.github.ramiroabadie.backend.diario.Diario;
import io.github.ramiroabadie.backend.diario.DiarioDeUsuario;
import io.github.ramiroabadie.backend.diario.EstadisticasDeDiario;
import io.github.ramiroabadie.backend.diario.GranularidadFecha;
import io.github.ramiroabadie.backend.diario.NuevoRegistro;
import io.github.ramiroabadie.backend.diario.ObrasPorAnio;
import io.github.ramiroabadie.backend.diario.OpinionesDeProduccion;
import io.github.ramiroabadie.backend.diario.ProduccionInexistenteException;
import io.github.ramiroabadie.backend.diario.RegistroAjenoException;
import io.github.ramiroabadie.backend.diario.RegistroDeDiario;
import io.github.ramiroabadie.backend.diario.RegistroInvalidoException;
import io.github.ramiroabadie.backend.diario.RegistroNoEncontradoException;
import io.github.ramiroabadie.backend.diario.ReseniaDeProduccion;
import io.github.ramiroabadie.backend.diario.internal.registro.RegistroRepository.Valoracion;

/**
 * Los casos de uso del diario (HU-09 a HU-14) y la única implementación de la interfaz pública
 * del módulo. Acá viven las dos reglas del dominio que no son obvias y que conviene no
 * "simplificar": la fecha difusa (MD-1) y el promedio por último rating (D20).
 *
 * <p>Es también el único lugar del sistema que invoca a Catálogo: para validar que la producción
 * que se registra existe y para traer títulos. Esa es la única dependencia módulo-a-módulo
 * permitida (MODULE_MAP).</p>
 */
@Service
class RegistroService implements Diario {

	/** Lo que entra en la columna. Una reseña de teatro larguísima sigue entrando cómoda. */
	private static final int MAXIMO_CARACTERES_RESENIA = 5000;

	private final RegistroRepository repositorio;

	private final CatalogoProducciones catalogo;

	RegistroService(RegistroRepository repositorio, CatalogoProducciones catalogo) {
		this.repositorio = repositorio;
		this.catalogo = catalogo;
	}

	@Override
	@Transactional
	public RegistroDeDiario registrar(Long usuarioId, NuevoRegistro nuevo) {
		ProduccionBasica produccion = produccionDe(nuevo.produccionId());
		Registro registro = new Registro(usuarioId, produccion.id(), fechaNormalizada(nuevo),
				nuevo.granularidad(), ratingValidado(nuevo.rating()), textoLimpio(nuevo.resenia()));
		return describir(repositorio.save(registro), produccion);
	}

	@Override
	@Transactional
	public RegistroDeDiario editar(Long usuarioId, Long registroId, NuevoRegistro cambios) {
		Registro registro = propio(usuarioId, registroId);
		ProduccionBasica produccion = produccionDe(cambios.produccionId());
		registro.actualizar(produccion.id(), fechaNormalizada(cambios), cambios.granularidad(),
				ratingValidado(cambios.rating()), textoLimpio(cambios.resenia()));
		return describir(registro, produccion);
	}

	@Override
	@Transactional
	public void borrar(Long usuarioId, Long registroId) {
		repositorio.delete(propio(usuarioId, registroId));
	}

	/**
	 * El diario completo, sin paginar: es el historial de una persona, no un feed. Las
	 * estadísticas salen de los mismos registros que ya están en memoria (HU-13).
	 */
	@Override
	@Transactional(readOnly = true)
	public DiarioDeUsuario deUsuario(Long usuarioId) {
		List<Registro> conFecha = repositorio.findByUsuarioIdAndFechaIsNotNullOrderByFechaDescCreadoEnDesc(usuarioId);
		List<Registro> sinFecha = repositorio.findByUsuarioIdAndFechaIsNullOrderByCreadoEnDesc(usuarioId);
		Map<Long, ProduccionBasica> producciones = catalogo.porIds(
				todos(conFecha, sinFecha).map(Registro::getProduccionId).distinct().toList());
		return new DiarioDeUsuario(describir(conFecha, producciones), describir(sinFecha, producciones),
				estadisticas(conFecha, sinFecha));
	}

	@Override
	@Transactional(readOnly = true)
	public OpinionesDeProduccion opinionesDe(Long produccionId) {
		Valoracion valoracion = repositorio.valoracionDe(produccionId);
		List<ReseniaDeProduccion> resenias = repositorio
				.findByProduccionIdAndReseniaIsNotNullOrderByCreadoEnDesc(produccionId).stream()
				.map(RegistroService::resenia)
				.toList();
		return new OpinionesDeProduccion(conUnDecimal(valoracion.getPromedio()), valoracion.getCantidad(), resenias);
	}

	/**
	 * Normaliza la fecha al comienzo del período que nombra y la valida contra su granularidad.
	 * Rechaza el futuro sobre el período ya normalizado: "agosto de 2026" en julio de 2026 es
	 * una función que todavía no pasó, pero "2026" a secas es perfectamente válido.
	 */
	private LocalDate fechaNormalizada(NuevoRegistro datos) {
		GranularidadFecha granularidad = datos.granularidad();
		if (granularidad == null) {
			throw new RegistroInvalidoException("granularidad", "Falta decir con qué precisión está la fecha");
		}
		if (granularidad == GranularidadFecha.SIN_FECHA) {
			if (datos.fecha() != null) {
				throw new RegistroInvalidoException("fecha", "Un registro sin fecha no lleva fecha");
			}
			return null;
		}
		if (datos.fecha() == null) {
			throw new RegistroInvalidoException("fecha", "Falta la fecha, o elegí \"no me acuerdo\"");
		}
		LocalDate comienzo = switch (granularidad) {
			case ANIO -> datos.fecha().withDayOfYear(1);
			case MES -> datos.fecha().withDayOfMonth(1);
			default -> datos.fecha();
		};
		if (comienzo.isAfter(LocalDate.now())) {
			throw new RegistroInvalidoException("fecha", "Esa función todavía no pasó");
		}
		return comienzo;
	}

	private Integer ratingValidado(Integer rating) {
		if (rating != null && (rating < 1 || rating > 10)) {
			throw new RegistroInvalidoException("rating", "El puntaje va de 1 a 10");
		}
		return rating;
	}

	/** Una reseña en blanco es no haber escrito reseña, no una reseña vacía. */
	private String textoLimpio(String resenia) {
		if (resenia == null || resenia.isBlank()) {
			return null;
		}
		String limpio = resenia.strip();
		if (limpio.length() > MAXIMO_CARACTERES_RESENIA) {
			throw new RegistroInvalidoException("resenia", "La reseña es demasiado larga");
		}
		return limpio;
	}

	private ProduccionBasica produccionDe(Long produccionId) {
		if (produccionId == null) {
			throw new RegistroInvalidoException("produccionId", "Falta decir qué producción viste");
		}
		return catalogo.porId(produccionId)
				.orElseThrow(() -> new ProduccionInexistenteException(produccionId));
	}

	/** La autorización de dueño (D30): el registro existe, pero no es de quien lo quiere tocar. */
	private Registro propio(Long usuarioId, Long registroId) {
		Registro registro = repositorio.findById(registroId)
				.orElseThrow(() -> new RegistroNoEncontradoException(registroId));
		if (!registro.esDe(usuarioId)) {
			throw new RegistroAjenoException(registroId);
		}
		return registro;
	}

	private EstadisticasDeDiario estadisticas(List<Registro> conFecha, List<Registro> sinFecha) {
		long producciones = todos(conFecha, sinFecha).map(Registro::getProduccionId).distinct().count();
		OptionalDouble promedio = todos(conFecha, sinFecha)
				.filter(registro -> registro.getRating() != null)
				.mapToInt(Registro::getRating)
				.average();
		TreeMap<Integer, Long> porAnio = conFecha.stream().collect(Collectors.groupingBy(
				registro -> registro.getFecha().getYear(), TreeMap::new, Collectors.counting()));
		return new EstadisticasDeDiario(
				conFecha.size() + (long) sinFecha.size(),
				producciones,
				promedio.isPresent() ? conUnDecimal(promedio.getAsDouble()) : null,
				sinFecha.size(),
				porAnio.descendingMap().entrySet().stream()
						.map(anio -> new ObrasPorAnio(anio.getKey(), anio.getValue()))
						.toList());
	}

	private Stream<Registro> todos(List<Registro> conFecha, List<Registro> sinFecha) {
		return Stream.concat(conFecha.stream(), sinFecha.stream());
	}

	private List<RegistroDeDiario> describir(List<Registro> registros, Map<Long, ProduccionBasica> producciones) {
		return registros.stream()
				.map(registro -> describir(registro, producciones.get(registro.getProduccionId())))
				.toList();
	}

	private RegistroDeDiario describir(Registro registro, ProduccionBasica produccion) {
		return new RegistroDeDiario(registro.getId(), produccion, registro.getFecha(),
				registro.getGranularidad(), registro.getRating(), registro.getResenia(), registro.getCreadoEn());
	}

	private static ReseniaDeProduccion resenia(Registro registro) {
		return new ReseniaDeProduccion(registro.getId(), registro.getUsuarioId(), registro.getResenia(),
				registro.getRating(), registro.getFecha(), registro.getGranularidad(), registro.getCreadoEn());
	}

	/** Los promedios se muestran con decimales (D9); uno alcanza y sobra para un 1-10 (HU-14). */
	private static Double conUnDecimal(Double promedio) {
		return promedio == null ? null : BigDecimal.valueOf(promedio)
				.setScale(1, RoundingMode.HALF_UP).doubleValue();
	}
}
