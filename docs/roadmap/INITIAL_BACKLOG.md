# Initial Backlog

> Estado: v1.0 — el primer backlog ejecutable. Detalle fino solo para la Fase 0 y arranque
> de Fase 1 (detallar el futuro lejano es desperdicio: cambiará). Los ítems se convierten
> en issues de GitHub al crear el repo.

## El primer incremento desarrollable (definición exacta)

**"El esqueleto que camina":** una URL pública con HTTPS donde una página Next.js
renderizada en servidor muestra un dato leído de PostgreSQL a través de un endpoint de
Spring Boot, todo corriendo en Docker Compose en el VPS, con CI verde en GitHub Actions.

Cuando esto existe, el proyecto ya "está en producción" — todo lo que sigue es agregarle
producto a una tubería probada, en vez de acumular producto local y rezar en el deploy final.

## Fase 0 — issues

1. Crear monorepo: estructura `/backend`, `/frontend`, `/docs`, licencia AGPL-3.0,
   `.gitignore`, `.env.example`, README mínimo (qué es el proyecto, cómo levantarlo).
2. Migrar esta documentación fundacional (`docs/`) al repo. Los docs viven con el código.
3. Backend: proyecto Spring Boot (Java 21, Maven) con paquetes por módulo
   (`identidad`, `catalogo`, `diario`, `social`, `aplicacion`) y un endpoint de salud
   que lee de Postgres.
4. Frontend: proyecto Next.js con una página SSR que consume el endpoint de salud.
5. `docker-compose.yml`: spring + next + postgres + caddy, con volúmenes para datos e
   imágenes. Levanta local con un comando.
6. CI: GitHub Actions con build + tests de ambas apps en cada PR.
7. Contratar VPS + dominio. Deploy del Compose. HTTPS por Caddy. UptimeRobot configurado.
8. Documentar el procedimiento de deploy en `docs/operations/DEPLOY.md` (para vos-en-6-meses).

## Fase 1 — issues iniciales (se refinan al llegar)

9. Identidad: entidad Usuario, registro con email + password, login con sesiones
   (Spring Security, cookie HTTP-only), logout.
10. Catálogo: entidades Producción, Persona, Participación, Sala + migraciones
    (propiedad de tablas por módulo documentada en el código).
11. Panel admin: CRUD de salas (el catálogo más simple primero — sirve de molde para el resto).
12. Panel admin: CRUD de producciones con participaciones (buscar-o-crear personas),
    subida de afiche con redimensionado, estados.
13. Ficha pública de producción: SSR + Open Graph (título, afiche, sinopsis).
    **Test de aceptación literal: pegar el link en WhatsApp y ver el preview.**
14. Página de artista (nombre + participaciones) y vista "en cartel".
15. [curaduría, en paralelo] Carga de salas de CABA + primeras 10 fichas reales.

## Épicas de Fases 2-4 (sin desglosar todavía)

- E1: El gesto de registro completo (D18-D20) — la épica más importante del producto.
- E2: Perfil/diario + stats mínimas.
- E3: Búsqueda pg_trgm.
- E4: Follow + feed compuesto + likes.
- E5: Sugerencias + reportes (las dos colas del admin).
- E6: Backups probados + endurecimiento.
- E7: Beta cerrada + correcciones.

## Definición de "hecho" (para cada issue)

Código en main vía PR con CI verde + funciona en el VPS (no solo local) + si tocó una
decisión del log, el log está actualizado.
