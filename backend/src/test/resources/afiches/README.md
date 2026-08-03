# Archivos de prueba de afiches

Tres archivos que **no se pueden fabricar desde Java** en el test y por eso están versionados,
con el mismo criterio que la fuente de D85: el binario, y al lado el comando exacto que lo
genera.

| Archivo | Qué prueba |
|---|---|
| `afiche.webp` | que el lector de WebP de D88 hace lo suyo: el JDK no lee WebP, y hoy cualquier imagen bajada de una web moderna llega así |
| `afiche-orientado.webp` | que la orientación EXIF se aplica **también en WebP**, que la guarda en un bloque `EXIF` de su contenedor RIFF y sin el prefijo `Exif\0\0` del JPEG |
| `afiche-cmyk.jpg` | que un JPEG CMYK —lo que sale de imprenta, y lo que llega en un afiche de teatro— entra: el lector del JDK se planta con esos |

El JPEG con EXIF y el PNG con `eXIf` **no están acá a propósito**: esos el test los arma a mano,
porque insertar un bloque en un contenedor es justo lo que se quiere dejar escrito.

Regenerarlos (necesita Pillow con soporte WebP):

```sh
python3 - <<'PY'
from PIL import Image

def degrade(w, h):
    img = Image.new("RGB", (w, h))
    px = img.load()
    for x in range(w):
        for y in range(h):
            px[x, y] = (x % 256, (x * 2) % 256, 128)
    return img

degrade(400, 600).save("afiche.webp", "WEBP", quality=80)
exif = b"Exif\x00\x00" + b"MM\x00\x2a\x00\x00\x00\x08\x00\x01\x01\x12\x00\x03\x00\x00\x00\x01\x00\x06\x00\x00\x00\x00\x00\x00"
degrade(40, 20).save("afiche-orientado.webp", "WEBP", quality=80, exif=exif)
degrade(400, 600).convert("CMYK").save("afiche-cmyk.jpg", "JPEG", quality=85)
PY
```
