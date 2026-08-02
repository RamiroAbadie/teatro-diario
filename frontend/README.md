# frontend

Next.js (App Router) + Tailwind. Es la mitad visible de [teatro-diario](../README.md); el
backend es Spring Boot y vive en [`../backend`](../backend).

## Correrlo

Necesita el backend arriba (ver el README de la raíz). Después:

```bash
npm install
npm run dev
```

Queda en `http://localhost:3000`. **Se usa npm** —hay un solo `package-lock.json` en el
repo— y **el deploy es Docker Compose + Caddy sobre un VPS** (Fase 5), no una plataforma
gestionada.

En desarrollo no hay Caddy, así que `next.config.ts` reenvía `/api` a Spring con un rewrite:
el navegador ve **un solo origen**, igual que en producción, y la cookie de sesión y el token
CSRF se comportan como se van a comportar de verdad.

## Antes de escribir una pantalla

Cuatro documentos, y no son opcionales — dicen dónde va cada cosa, así que cuando aparece
algo imprevisto no hay que decidirlo en el momento:

| | |
|---|---|
| **Dónde va cada cosa** en esta carpeta | [`FRONTEND_ARCHITECTURE.md`](../docs/architecture/FRONTEND_ARCHITECTURE.md) |
| **El contrato con el backend** | [`API.md`](../docs/architecture/API.md) |
| **Tokens y componentes** | [`DESIGN_SYSTEM.md`](../docs/product/DESIGN_SYSTEM.md) |
| **Cada pantalla, una por una** | [`SCREEN_SPECS.md`](../docs/product/SCREEN_SPECS.md) |

Y el porqué de todo está en
[`DECISION_LOG.md`](../docs/decisions/DECISION_LOG.md), que es la fuente de verdad.

## Las cinco reglas que más caro sale romper

1. **Ninguna lógica de negocio acá.** El frontend pide, muestra y manda.
2. **El `fetch` vive sólo en `lib/api/`.** Cero `fetch` en un componente o en una página.
3. **Server Component por defecto**; `"use client"` es la excepción y se justifica.
4. **Lo que decide la caché es la llamada, no el endpoint.** Sin cookie, TTL; con cookie,
   jamás. Confundirlos sirve la página de una persona a otra.
5. **Las mutaciones las hace el navegador contra Spring.** Sin Server Actions.

## Dos cosas de la estructura que no se ven solas

- **`app/(sitio)/` es la frontera del armazón.** El layout raíz es `<html>`/`<body>` y nada
  más, porque los layouts del App Router se anidan y el panel admin **no lleva armazón**
  (D81): si estuviera arriba, no habría forma de sacárselo. Los paréntesis no cambian la URL.
- **`lib/api/<modulo>.servidor.ts` y `.cliente.ts`** (D82): el nombre del archivo dice desde
  dónde se puede importar. Un archivo con las dos mitades no compila.

## Dependencias

Next, React, TypeScript y **Tailwind, que es la única dependencia de UI** (D73). No hay
librería de componentes, ni de íconos, ni de estado, ni de data-fetching, ni webfonts. Cada
una vuelve a estar sobre la mesa cuando aparezca su problema concreto, con una decisión en
el log (D51).
