/**
 * ⏳ **Provisorio.** Esta ruta es la pantalla 1 (home visitante) sin sesión y la 2 (feed)
 * con sesión, y las dos se escriben después: la 1 en el paso 2 de la Fase 4 y la 2 en el
 * paso 5. Lo que hay acá es lo mínimo para que el armazón tenga qué envolver.
 */
export default function Provisoria() {
  return (
    <div className="max-w-3xl">
      <h1 className="font-titulo text-3xl">El diario de tu teatro de acá en adelante</h1>
      <p className="mt-4 max-w-[65ch] text-tinta-suave">
        El esqueleto del frontend está en pie: los tokens, el armazón de cuatro piezas y los
        dos clientes de la API. Las pantallas entran una por una a partir del paso 2 de la
        Fase 4, empezando por las públicas con SSR y Open Graph.
      </p>
    </div>
  );
}
