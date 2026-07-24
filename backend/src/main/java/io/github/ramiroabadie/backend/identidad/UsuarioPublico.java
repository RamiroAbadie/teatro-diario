package io.github.ramiroabadie.backend.identidad;

import java.time.Instant;

/**
 * La cara pública de una cuenta: la que ve cualquiera, con cuenta o sin ella (D21). Sin email:
 * eso lo ve solo su dueño y viaja en la respuesta de {@code /api/auth/yo}.
 *
 * <p>El {@code id} es la referencia opaca con la que Diario y Social guardan lo suyo (D30); el
 * {@code username} es la URL del perfil (MD-4).</p>
 */
public record UsuarioPublico(Long id, String username, Instant creadoEn) {
}
