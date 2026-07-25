package io.github.ramiroabadie.backend.aplicacion.internal.social;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import io.github.ramiroabadie.backend.diario.CursorDeActividad;
import io.github.ramiroabadie.backend.diario.RegistroDeDiario;

/**
 * El cursor del feed tal como viaja por HTTP: {@code <instante-de-carga>_<idDelRegistro>}, que es
 * el último ítem entregado. El cliente lo recibe en la respuesta y lo devuelve como
 * {@code ?cursor=} para pedir la página siguiente; no lo arma él ni tiene por qué entenderlo.
 *
 * <p>Traducirlo es trabajo de esta capa y no del módulo: Diario lo recibe tipado
 * ({@link CursorDeActividad}) y no sabe que existe una cadena de texto. Va legible y sin
 * codificar porque no esconde nada —los dos datos están en la respuesta— y porque un cursor que
 * se puede leer se puede depurar con curl.</p>
 */
final class CursorDelFeed {

	private static final String SEPARADOR = "_";

	private CursorDelFeed() {
	}

	static String codificar(RegistroDeDiario ultimo) {
		return ultimo.creadoEn() + SEPARADOR + ultimo.id();
	}

	/** Sin cursor es la primera página. Con uno roto es 400: adivinar dónde quería seguir es peor. */
	static CursorDeActividad decodificar(String texto) {
		if (texto == null || texto.isBlank()) {
			return null;
		}
		int corte = texto.lastIndexOf(SEPARADOR);
		if (corte < 0) {
			throw new CursorInvalidoException();
		}
		try {
			return new CursorDeActividad(Instant.parse(texto.substring(0, corte)),
					Long.parseLong(texto.substring(corte + 1)));
		}
		catch (DateTimeParseException | NumberFormatException ex) {
			throw new CursorInvalidoException();
		}
	}
}
