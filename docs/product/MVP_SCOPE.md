# MVP Scope

> Estado: v1.0 — cierre de Etapa 4. **El MVP queda congelado.**
> Regla aplicada (P2): una funcionalidad entra solo si es necesaria para probar la propuesta
> central (S1: la gente quiere registrar y compartir su teatro) o para que el loop funcione.
> Cambiar este alcance requiere una decisión explícita en el Decision Log, no un "ya que estamos".

## Imprescindible — el MVP

| # | Funcionalidad | Justificación |
|---|---|---|
| 1 | Cuentas de usuario y perfil público con el **diario** como vista central | El núcleo (D2) |
| 2 | **Registro** en un gesto: producción + fecha opcional/aproximada + rating opcional (1–10) + reseña opcional. Re-visto permitido. Editar/borrar el propio registro | La acción central (D18–D19) |
| 3 | Promedio por producción sobre el último rating de cada usuario | D20 — no es AVG plano |
| 4 | **Catálogo**: fichas de producciones (con estado en cartel/cerrada/próximamente), personas (nombre + participaciones) y salas | Contra qué se registra (D7, D12–D15) |
| 5 | **Panel de admin** para curar catálogo: CRUD de producciones, personas, salas; aprobación de sugerencias | Sin esto el catálogo no existe. Producto interno, calidad de "herramienta", no de producto |
| 6 | **Sugerencias** de producciones faltantes (formulario → ficha semi-armada → aprobación) | Válvula del historial viejo (D24). Sin esto, el usuario intensivo choca contra una pared |
| 7 | **Follow** + **feed de actividad** de seguidos, con fallback de actividad global | La capa social mínima (D3, D22) |
| 8 | **Likes** a reseñas | D11 |
| 9 | **Búsqueda simple** (texto) sobre producciones, personas y usuarios | D23 |
| 10 | Vista **"en cartel"** | Descubrimiento sin algoritmo |
| 11 | **Estadísticas mínimas** en el perfil: obras vistas por año, promedio de ratings propios, y poco más — solo queries sobre los registros propios | Alcance quirúrgico (D26). Sin gráficos elaborados |

## Importante después del MVP (v1.1+)

- **Listas** (personal, pública, orden manual) — el loop cierra sin ellas; feature perfecta post-lanzamiento (D25).
- **Comentarios** en reseñas (X5) — cuando haya conversación que moderar.
- **Links externos a entradas** (D6).
- Compartir registro **como imagen para stories** (X3) — convivir con Instagram, no competirle.
- **Roles técnicos** en fichas (D17).
- **Scraper de Alternativa Teatral** como herramienta interna de precarga para el admin (P4) — va a hacer falta rápido, pero el MVP arranca con carga manual.
- "Tu año en teatro" estilo Wrapped — feature de retención para diciembre.

## Futuro / descartado por ahora

Ver Decision Log: gamificación de conversión (X1), recomendación algorítmica (X2),
agenda de funciones (X4), venta de entradas, monetización, app móvil (D5: web primero),
niveles de privacidad (D21), entidad Obra normalizada (D13), festivales como entidad.

## Qué prueba este MVP

- **S1**: ¿la gente registra? → métrica honesta: usuarios que registran ≥3 obras y vuelven.
- **S2**: ¿lo social mínimo aporta? → follows por usuario, likes, visitas a feed.
- **S3**: ¿el hábito migra de Instagram? → frecuencia de registro vs. asistencia declarada.

Si S1 falla, ninguna feature adicional lo arregla: el problema sería la propuesta, no el producto.
