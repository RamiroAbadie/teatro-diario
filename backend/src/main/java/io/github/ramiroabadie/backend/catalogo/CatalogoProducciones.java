package io.github.ramiroabadie.backend.catalogo;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * La interfaz pública del módulo Catálogo: lo único que otro módulo puede invocar (MODULE_MAP).
 * Existe para sostener la única dependencia módulo-a-módulo del sistema, Diario → Catálogo:
 * validar que la producción que se registra existe y leer su título para mostrarlo.
 *
 * <p>Devuelve {@link ProduccionBasica} y no la entidad: la identidad de las entidades teatrales
 * es autoridad de Catálogo y nadie de afuera la modifica.</p>
 */
public interface CatalogoProducciones {

	/** Vacío si la producción no existe: es la validación del registro (HU-09). */
	Optional<ProduccionBasica> porId(Long id);

	/** Los títulos de un diario entero en una sola consulta. Los ids que no existen no vienen. */
	Map<Long, ProduccionBasica> porIds(Collection<Long> ids);
}
