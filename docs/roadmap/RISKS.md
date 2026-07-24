# Risks

> Registro honesto de los riesgos del proyecto, en orden de gravedad. Se revisa al cierre
> de cada fase del roadmap.

| # | Riesgo | Gravedad | Mitigación vigente |
|---|--------|----------|-------------------|
| R1 | **La demanda no existe** (S1 falso): el fundador no es usuario del producto y la validación fue informal | Alta — mata el proyecto | MVP mínimo (menos pérdida si falla); beta cerrada con espectadores reales en Fase 4 con métricas definidas (registran 3+ obras y vuelven); pivote o cierre informado si la beta falla |
| R2 | **El catálogo como cuello de botella**: el admin es la base de datos; el catálogo se pudre en silencio | Alta — rompe el loop | 50 fichas impecables (D38); sugerencias bajo demanda (D24); rutina fija semanal de curaduría (D37); escalera de fuentes (D39) si la manual no alcanza |
| R3 | **Abandono por agotamiento**: 8 hs/semana durante 4-6 meses, en paralelo a trabajo y facultad, sin feedback externo hasta la Fase 4 | Media-alta | Fases con criterios de salida visibles; esqueleto en producción desde la semana 3 (motivación de "ya está online"); alcance negociable, calidad de límites no |
| R4 | **Deuda operativa**: primera vez operando producción; un incidente (disco lleno, DB corrupta, VPS comprometido) sin preparación | Media | Deudas de aprendizaje explícitas y pagadas temprano (Fase 0); backups con restauración probada antes de usuarios reales (D45); actualizaciones de seguridad del VPS en la rutina semanal |
| R5 | **Hábito que no migra** (S3 falso): el público sigue en Instagram aunque el producto exista | Media | La vara de fricción del registro (P8); futuro "compartir como imagen" para convivir en vez de competir (X3); se mide en beta |
| R6 | **Contenido problemático**: reseñas que difaman a personas reales nombradas en el catálogo | Media — legal/reputacional | Reportar + borrado por admin (D40); todo público facilita detección; revisión de reportes en la rutina semanal |
| R7 | **La capa social vacía** (S2 sobreestimado): follows y feed sin masa crítica que los justifique | Baja — el diario sobrevive solo (P1) | El fallback global (D22) disimula el vacío; el valor individual no depende de lo social |
| R8 | **Scraping legal** (si se llega al peldaño 2 de fuentes) | Baja hoy — diferido | La decisión se toma en su momento con revisión de términos (D39); datos fácticos + sinopsis propias reducen exposición |
| R9 | **Carrera en la fusión de duplicados** (D63): entre que se mudan los registros y se borra la ficha hay una ventana de milisegundos; si alguien registra esa ficha justo ahí, su registro queda apuntando a la ficha borrada | Baja — **riesgo aceptado** | Ninguna, a propósito: cerrarla pide bloqueos pesimistas coordinados entre módulos, que es mucha maquinaria para una ventana de milisegundos con un solo admin fusionando una vez por semana (mismo criterio que ADR-002: el peldaño se sube ante un problema concreto). El daño está acotado por D62 — el registro sobrevive legible, con su título, y solo pierde el link — y se repara con un `UPDATE` a mano, como la promoción a admin. **Revisar si:** hay más de un admin, si la fusión se automatiza, o la primera vez que pase de verdad |
