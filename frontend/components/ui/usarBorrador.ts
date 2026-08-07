"use client";

import { useCallback, useEffect, useRef, useState } from "react";

/**
 * El borrador que sobrevive. USER_FLOWS lo pide dos veces —"el contenido tipeado NO se
 * pierde", "vuelve a donde estaba con lo tipeado"— y `SCREEN_SPECS.md` lo baja a una regla:
 * **los dos formularios que importan —el gesto y la sugerencia— guardan su borrador en
 * `sessionStorage` mientras se escribe y lo restauran al montarse. Se borra al publicar.**
 *
 * Es una API del navegador, no una herramienta (D51).
 *
 * ⚠️ **Se lee DESPUÉS de montar, y eso no contradice lo que D79 descartó para el tema
 * oscuro**: aquel caso necesitaba leerse *antes del primer pintado* para evitar un destello,
 * que con SSR produce desajustes de hidratación. Éste no afecta el primer pintado y su peor
 * caso es un campo que se completa 50 ms tarde.
 *
 * ⚠️ **Y no se escribe antes de haber leído**: guardar el estado inicial en el primer render
 * pisaría el borrador que se estaba por restaurar, que es la forma exacta de que esta pieza
 * borre lo que vino a salvar.
 */
export function usarBorrador<T extends object>(clave: string, inicial: T) {
  const [valor, setValor] = useState<T>(inicial);
  const primerRender = useRef(true);
  /**
   * ⚠️ **Lo que hace que `olvidar()` realmente olvide.** Quien publica hace dos cosas
   * seguidas —olvidar y vaciar el formulario—, y el vaciado es un cambio de estado como
   * cualquier otro: el efecto de abajo corría detrás y **volvía a guardar el borrador que se
   * acababa de borrar**. Medido en el navegador: después de publicar, `sessionStorage` tenía
   * un borrador vacío con la fecha del día en que se publicó. Con esto, la escritura que
   * sigue a un `olvidar()` se saltea, y la siguiente tecla del usuario vuelve a guardar.
   */
  const omitirProximaEscritura = useRef(false);

  useEffect(() => {
    const guardado = sessionStorage.getItem(clave);
    if (!guardado) return;
    try {
      // Con lo que ya hay de base: un borrador viejo al que le falte un campo nuevo no deja
      // el formulario con un `undefined` adentro.
      setValor((actual) => ({ ...actual, ...(JSON.parse(guardado) as Partial<T>) }));
    } catch {
      // Un borrador ilegible no es un error que mostrar: se descarta y se sigue.
      sessionStorage.removeItem(clave);
    }
    // `inicial` a propósito fuera de las dependencias: es la semilla del primer render, no
    // algo a lo que este efecto tenga que reaccionar.
  }, [clave]);

  useEffect(() => {
    // El primer render no escribe: guardar el estado inicial antes de haber leído pisaría
    // el borrador que se está por restaurar, que es la forma exacta de que esta pieza borre
    // lo que vino a salvar.
    if (primerRender.current) {
      primerRender.current = false;
      return;
    }
    if (omitirProximaEscritura.current) {
      omitirProximaEscritura.current = false;
      return;
    }
    sessionStorage.setItem(clave, JSON.stringify(valor));
  }, [clave, valor]);

  /** Al publicar con éxito: lo que ya se envió no es un borrador. */
  const olvidar = useCallback(() => {
    omitirProximaEscritura.current = true;
    sessionStorage.removeItem(clave);
  }, [clave]);

  return { valor, setValor, olvidar };
}
