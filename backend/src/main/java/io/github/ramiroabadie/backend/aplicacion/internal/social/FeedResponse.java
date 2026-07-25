package io.github.ramiroabadie.backend.aplicacion.internal.social;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import io.github.ramiroabadie.backend.diario.ActividadDeDiario;
import io.github.ramiroabadie.backend.diario.GranularidadFecha;
import io.github.ramiroabadie.backend.diario.ProduccionRegistrada;
import io.github.ramiroabadie.backend.diario.RegistroDeDiario;
import io.github.ramiroabadie.backend.identidad.UsuarioPublico;

/**
 * Una página del feed (HU-16): qué registraron los que seguís, de lo más nuevo a lo más viejo.
 *
 * @param global {@code true} cuando esto no es el feed de nadie sino la actividad de toda la
 * plataforma, porque quien mira todavía no sigue a nadie (D22). Viaja en la respuesta porque la
 * pantalla tiene que poder explicar qué está viendo y ofrecer seguir gente (USER_FLOWS, flujo 2):
 * un feed ajeno sin aviso se lee como un feed roto
 * @param siguienteCursor lo que hay que mandar como {@code ?cursor=} para seguir leyendo, o
 * {@code null} cuando no hay más. Una página llena siempre trae cursor aunque justo sea la
 * última: averiguarlo pediría una fila más en cada consulta, y el costo de equivocarse es un
 * pedido de más que devuelve vacío
 */
record FeedResponse(boolean global, List<Item> items, String siguienteCursor) {

	static FeedResponse desde(boolean global, List<ActividadDeDiario> actividad,
			Map<Long, UsuarioPublico> autores, int limite) {
		List<Item> items = actividad.stream()
				.map(linea -> Item.desde(linea, autores.get(linea.usuarioId())))
				.toList();
		String siguiente = actividad.size() < limite ? null
				: CursorDelFeed.codificar(actividad.get(actividad.size() - 1).registro());
		return new FeedResponse(global, items, siguiente);
	}

	/**
	 * Una línea del feed: la línea del diario de otro, firmada. Es plana y no anida el registro
	 * adentro porque acá el autor no es un dato más — es la mitad de la información (HU-16).
	 *
	 * <p>{@code fecha} y {@code granularidad} son cuándo vio la obra, que puede ser difusa
	 * (MD-1); {@code creadoEn} es cuándo lo contó, que es por donde ordena el feed.</p>
	 */
	record Item(
			Long registroId,
			String autor,
			ProduccionRegistrada produccion,
			LocalDate fecha,
			GranularidadFecha granularidad,
			Integer rating,
			String resenia,
			Instant creadoEn
	) {

		static Item desde(ActividadDeDiario actividad, UsuarioPublico autor) {
			RegistroDeDiario registro = actividad.registro();
			return new Item(registro.id(), autor == null ? null : autor.username(), registro.produccion(),
					registro.fecha(), registro.granularidad(), registro.rating(), registro.resenia(),
					registro.creadoEn());
		}
	}
}
