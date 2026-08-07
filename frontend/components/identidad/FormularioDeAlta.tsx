"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import type { FormEvent } from "react";

import { Aviso } from "@/components/ui/Aviso";
import { Boton } from "@/components/ui/Boton";
import { Campo } from "@/components/ui/Campo";
import { comoFormulario } from "@/lib/api/errores";
import { registro } from "@/lib/api/identidad.cliente";

/**
 * **Pantalla 11 · Alta de cuenta** (HU-01). El alta **deja la sesión abierta**: al terminar
 * no se pasa por el login, se va directo a donde estaba.
 *
 * Las reglas de los campos son las de `API.md` y **se validan acá antes de mandar**: el `400`
 * del backend es la red de contención, no la fuente del mensaje (D78). Dos que no son
 * decorativas:
 *
 * - **La advertencia del username va ANTES de enviar, no después**: es parte de la URL del
 *   perfil y **no se puede cambiar** (MD-4/D75). Enterarse al recibir el error es tarde.
 * - **El contador de la contraseña cuenta bytes UTF-8, no caracteres.** 72 es el límite real
 *   de BCrypt y **40 letras con tilde ya son 80 bytes**, así que contando caracteres el error
 *   aparecería recién al enviar, cuando ya no se ve el campo.
 */
export function FormularioDeAlta({ destino }: { destino: string }) {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const [errores, setErrores] = useState<Record<string, string>>({});

  const bytes = bytesUtf8(password);

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (enviando) return;

    const propios = validar(username, email, password, bytes);
    if (Object.keys(propios).length > 0) {
      setErrores(propios);
      setAviso(null);
      return;
    }

    setEnviando(true);
    setErrores({});
    setAviso(null);

    try {
      await registro({ username, email, password });
      // La sesión la resuelve el servidor en el layout: sin invalidar la caché del router,
      // el armazón seguiría dibujando al visitante que acaba de crear su cuenta.
      router.replace(destino);
      router.refresh();
    } catch (error) {
      // ⚠️ **El `409` tiene dos formas y las dos se dibujan**: con `errores` (el caso
      // normal: ese username o ese email ya estaban) va al lado del campo; **sin `errores`**
      // —la carrera que resuelve el índice único— va como mensaje general. En los dos casos
      // el formulario **se queda como está**: cambiar un username no puede costar volver a
      // escribir el email y la contraseña.
      const { general, campos } = comoFormulario(error);
      setErrores(campos);
      setAviso(general);
      setEnviando(false);
    }
  }

  return (
    <form
      // Ver `FormularioDeLogin`: sin hidratar, un `form` sin método manda los campos por la
      // URL — acá, la contraseña recién elegida.
      method="post"
      onSubmit={enviar}
      noValidate
      className="mt-6 space-y-5"
    >
      {aviso && <Aviso variante="error">{aviso}</Aviso>}

      <Campo
        id="username"
        etiqueta="Nombre de usuario"
        name="username"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        autoComplete="username"
        autoCapitalize="none"
        spellCheck={false}
        required
        error={errores.username}
        ayuda="Va a ser la dirección de tu perfil y no se puede cambiar después."
      />

      <Campo
        id="email"
        etiqueta="Email"
        type="email"
        name="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        autoComplete="email"
        autoCapitalize="none"
        spellCheck={false}
        required
        error={errores.email}
      />

      <Campo
        id="password"
        etiqueta="Contraseña"
        type="password"
        name="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        autoComplete="new-password"
        required
        error={errores.password}
        ayuda={
          // El contador aparece recién cuando hay algo escrito: en un campo vacío sería
          // ruido, y la regla que importa —el mínimo— ya está dicha en la misma línea.
          password === ""
            ? "Mínimo 8 caracteres."
            : `Mínimo 8 caracteres. Llevás ${bytes} de 72 lugares.`
        }
      />

      <Boton type="submit" cargando={enviando} etiquetaCargando="Creando cuenta…" className="w-full">
        Crear cuenta
      </Boton>
    </form>
  );
}

/**
 * Los mismos mensajes que devuelve el backend, a propósito: si el usuario ve uno acá y otro
 * al enviar, parecen dos reglas distintas.
 */
function validar(
  username: string,
  email: string,
  password: string,
  bytes: number,
): Record<string, string> {
  const errores: Record<string, string> = {};

  if (!/^[A-Za-z0-9_]{3,20}$/.test(username)) {
    errores.username = "Entre 3 y 20 caracteres: letras, números o guión bajo";
  }
  // Deliberadamente laxa: quien valida el email de verdad es el backend, y una expresión
  // regular ambiciosa acá sólo sirve para rechazar direcciones válidas y raras.
  if (!/^\S+@\S+\.\S+$/.test(email)) {
    errores.email = "El email no tiene un formato válido";
  } else if (email.length > 254) {
    errores.email = "El email no puede superar los 254 caracteres";
  }
  if (password.length < 8 || password.length > 72) {
    errores.password = "La contraseña necesita entre 8 y 72 caracteres";
  } else if (bytes > 72) {
    errores.password = "La contraseña es demasiado larga: los acentos y emojis ocupan más de un lugar";
  }

  return errores;
}

/** Lo que cuenta BCrypt. `TextEncoder` es del navegador: no es una dependencia (D51). */
function bytesUtf8(texto: string): number {
  return new TextEncoder().encode(texto).length;
}
