"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import type { FormEvent } from "react";

import { Aviso } from "@/components/ui/Aviso";
import { Boton } from "@/components/ui/Boton";
import { Campo } from "@/components/ui/Campo";
import { comoFormulario } from "@/lib/api/errores";
import { login } from "@/lib/api/identidad.cliente";

/**
 * **Pantalla 11 · Login** (HU-02). **Un solo campo `identificador`** —email o username— y la
 * etiqueta lo dice: quien entra no tiene por qué acordarse de con cuál se registró.
 *
 * ⚠️ **El `401` es un mensaje genérico y nunca dice cuál de los dos falló** (HU-02). Eso lo
 * decide el backend, que responde "Email/usuario o contraseña incorrectos", y acá se muestra
 * tal cual: no hay que deducir nada del código.
 *
 * ⚠️ **Y ese `401` no puede pasar por el manejador global**, que redirige al login: sería
 * recargar esta misma pantalla, perder lo tipeado y no mostrar nunca el mensaje. Por eso
 * `identidad.cliente.login()` lo pide con `devolver-el-error`.
 */
export function FormularioDeLogin({ destino }: { destino: string }) {
  const router = useRouter();
  const [identificador, setIdentificador] = useState("");
  const [password, setPassword] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const [errores, setErrores] = useState<Record<string, string>>({});

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (enviando) return;

    const propios: Record<string, string> = {};
    if (identificador.trim() === "") propios.identificador = "Ingresá tu email o nombre de usuario";
    if (password === "") propios.password = "Ingresá tu contraseña";
    if (Object.keys(propios).length > 0) {
      setErrores(propios);
      setAviso(null);
      return;
    }

    setEnviando(true);
    setErrores({});
    setAviso(null);

    try {
      await login({ identificador, password });
      // Igual que en el alta: el armazón lo dibuja el servidor, así que sin invalidar la
      // caché del router la pantalla de destino llegaría con la sesión vieja.
      router.replace(destino);
      router.refresh();
    } catch (error) {
      const { general, campos } = comoFormulario(error);
      setErrores(campos);
      setAviso(general);
      setEnviando(false);
    }
  }

  return (
    <form
      // ⚠️ **`method="post"` aunque el envío lo haga JavaScript.** Si el bundle todavía no
      // hidrató —o falló—, el navegador envía el formulario **nativamente**, y un `form` sin
      // método hace un `GET`: la contraseña termina en la URL, en el historial y en los logs
      // de cualquier proxy. Con `post` no viaja en la URL. Encontrado probando la pantalla en
      // un navegador de verdad, no leyendo el código.
      method="post"
      onSubmit={enviar}
      noValidate
      className="mt-6 space-y-5"
    >
      {aviso && <Aviso variante="error">{aviso}</Aviso>}

      <Campo
        id="identificador"
        etiqueta="Email o nombre de usuario"
        name="identificador"
        value={identificador}
        onChange={(e) => setIdentificador(e.target.value)}
        autoComplete="username"
        autoCapitalize="none"
        spellCheck={false}
        required
        error={errores.identificador}
      />

      <Campo
        id="password"
        etiqueta="Contraseña"
        type="password"
        name="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        autoComplete="current-password"
        required
        error={errores.password}
      />

      <Boton type="submit" cargando={enviando} etiquetaCargando="Entrando…" className="w-full">
        Entrar
      </Boton>
    </form>
  );
}
