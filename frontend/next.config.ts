import type { NextConfig } from "next";

/**
 * En producción el reparto lo hace Caddy y todo comparte origen. En desarrollo no hay
 * Caddy: Spring escucha en :8080 y Next en :3000, que son dos orígenes distintos — y con
 * dos orígenes la cookie de sesión y el CSRF no funcionan como en producción.
 *
 * El rewrite es el Caddy de los pobres: hace que el navegador vea un solo origen igual
 * que en producción (D78). Sólo aplica al NAVEGADOR; los Server Components llaman a la
 * URL interna por variable de entorno y no pasan por acá.
 *
 * ⚠️ `/afiches` NO lleva rewrite, y es una decisión y no un olvido: Spring no sirve esa
 * ruta ni la va a servir (D77). En desarrollo los afiches los escribe Spring dentro de
 * `frontend/public/afiches/` y los sirve Next como cualquier estático de `public/`.
 */
const nextConfig: NextConfig = {
  // El monorepo tiene más de un lockfile arriba y Turbopack infiere mal la raíz.
  // Acá se le dice cuál es: la del frontend, que es la única que le importa.
  turbopack: { root: __dirname },

  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${process.env.BACKEND_INTERNAL_URL ?? "http://localhost:8080"}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
