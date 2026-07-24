package io.github.ramiroabadie.backend.aplicacion.internal.curaduria;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.ramiroabadie.backend.catalogo.CatalogoProducciones;
import io.github.ramiroabadie.backend.catalogo.ProduccionBasica;
import io.github.ramiroabadie.backend.diario.Diario;

/**
 * Fusionar una ficha duplicada en la canónica (D63): el caso de uso que le faltaba a la curaduría
 * para que borrar un duplicado no le costara el historial a nadie.
 *
 * <p>Compone dos módulos que no se conocen —Diario muda los registros, Catálogo borra la ficha
 * vacía— y por eso vive acá (MODULE_MAP). Las dos cosas pasan en una sola transacción: una base,
 * un transaction manager (ARCHITECTURE.md), llamadas síncronas in-process (ADR-002). Si algo
 * falla, no queda ni media fusión hecha.</p>
 *
 * <p>La ficha destino gana entera: no se mezclan sinopsis, sala, elenco ni estado. El admin deja
 * la canónica como quiere y recién ahí fusiona; adivinar cuál de los dos textos era el bueno es
 * exactamente el tipo de magia que arruina un catálogo curado a mano.</p>
 *
 * <p>Límite conocido y aceptado (R9): la transacción no coordina con quien esté registrando al
 * mismo tiempo. Entre que se mudan los registros y se borra la ficha hay una ventana en la que
 * otro request puede validar que la duplicada existe e insertar un registro apuntándole; ese
 * registro se escapa de la mudanza y queda como los de D62 —legible, con su título, sin link—.
 * Cerrarla pide bloqueos pesimistas coordinados entre los dos módulos: mucha maquinaria para una
 * ventana de milisegundos con un solo admin fusionando una vez por semana.</p>
 */
@Service
class FusionDeProducciones {

	private final CatalogoProducciones catalogo;

	private final Diario diario;

	FusionDeProducciones(CatalogoProducciones catalogo, Diario diario) {
		this.catalogo = catalogo;
		this.diario = diario;
	}

	@Transactional
	FusionResponse fusionar(Long origenId, Long destinoId) {
		if (origenId.equals(destinoId)) {
			throw new FusionInvalidaException("Una producción no se fusiona consigo misma");
		}
		ProduccionBasica origen = ficha(origenId);
		ProduccionBasica destino = ficha(destinoId);
		int reasignados = diario.reasignarRegistros(origen.id(), destino.id());
		catalogo.borrar(origen.id());
		return new FusionResponse(destino.id(), destino.titulo(), reasignados);
	}

	private ProduccionBasica ficha(Long id) {
		return catalogo.porId(id).orElseThrow(() -> new FichaInexistenteException(id));
	}
}
