package io.github.ramiroabadie.backend.catalogo.internal.produccion;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.twelvemonkeys.imageio.metadata.Directory;
import com.twelvemonkeys.imageio.metadata.Entry;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEG;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEGSegment;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEGSegmentUtil;
import com.twelvemonkeys.imageio.metadata.tiff.TIFF;
import com.twelvemonkeys.imageio.metadata.tiff.TIFFReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * De lo que sube el admin a lo que se guarda: un JPEG que entra en 1200×1600, derecho y sin
 * metadatos. Es la parte de la subida que P16 dejó abierta y que cierra D88.
 *
 * <p>Tres cosas que no son obvias y son el motivo de que esta clase exista:</p>
 * <ul>
 * <li><b>Se leen las dimensiones antes de decodificar.</b> El tope de 5 MB es del archivo
 * comprimido y no acota la memoria: un PNG de pocos KB puede declarar 30.000×30.000 y reventar
 * el heap del VPS al decodificarse. Es lo único de esta clase que es seguridad y no calidad.</li>
 * <li><b>La orientación EXIF se aplica.</b> Una foto de celular sin rotar se guarda acostada, y
 * "acostada" en un afiche vertical es la ficha rota.</li>
 * <li><b>Nunca agranda.</b> Un afiche chico se guarda como vino: estirarlo no agrega
 * información y sí agrega peso.</li>
 * </ul>
 *
 * <p>La caja de 1200×1600 sale de D79: el lado mayor que se llega a mostrar es 1200 px (la placa
 * de {@code og:image}) y la ficha muestra el afiche entero sin recortar, así que **acá no se
 * recorta nada** — el recorte 2:3 de la grilla lo hace CSS sobre el mismo archivo.</p>
 */
@Component
class ProcesadorDeAfiche {

	private static final Logger log = LoggerFactory.getLogger(ProcesadorDeAfiche.class);

	/** Lo que se acepta subir (D77). Los tres se leen; ninguno se guarda como vino. */
	private static final Set<String> FORMATOS = Set.of("jpeg", "jpg", "png", "webp");

	/** La caja de destino (D79). Se encaja adentro, sin recortar y sin deformar. */
	private static final int ANCHO_MAXIMO = 1200;

	private static final int ALTO_MAXIMO = 1600;

	/**
	 * Calidad del JPEG de salida. 0,82 es donde la escala deja de comprarse artefactos visibles
	 * en las tipografías impresas adentro del afiche, que es lo que más sufre al recomprimir.
	 */
	private static final float CALIDAD = 0.82f;

	private final long maximoPixeles;

	ProcesadorDeAfiche(@Value("${afiches.maximo-pixeles}") long maximoPixeles) {
		this.maximoPixeles = maximoPixeles;
	}

	/**
	 * @throws AficheInvalidoException si está vacío, si el formato no es uno de los tres, o si la
	 * imagen declara más píxeles de los que estamos dispuestos a decodificar
	 */
	byte[] aJpeg(byte[] original) {
		if (original == null || original.length == 0) {
			throw new AficheInvalidoException("El archivo está vacío");
		}
		try (ImageInputStream entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(original))) {
			ImageReader lector = lectorDe(entrada);
			try {
				BufferedImage imagen = decodificar(lector);
				imagen = orientar(imagen, orientacionExif(original, lector.getFormatName()));
				return codificar(encajar(imagen));
			}
			finally {
				lector.dispose();
			}
		}
		catch (IOException ex) {
			throw new AficheInvalidoException("No pudimos leer esa imagen");
		}
	}

	private ImageReader lectorDe(ImageInputStream entrada) throws IOException {
		Iterator<ImageReader> lectores = ImageIO.getImageReaders(entrada);
		if (!lectores.hasNext()) {
			throw new AficheInvalidoException("Ese archivo no es una imagen JPEG, PNG o WebP");
		}
		ImageReader lector = lectores.next();
		lector.setInput(entrada);
		String formato = lector.getFormatName().toLowerCase(Locale.ROOT);
		if (!FORMATOS.contains(formato)) {
			lector.dispose();
			throw new AficheInvalidoException("Ese archivo no es una imagen JPEG, PNG o WebP");
		}
		return lector;
	}

	/** El tope se comprueba con la cabecera, que es barata, y no con la imagen ya en memoria. */
	private BufferedImage decodificar(ImageReader lector) throws IOException {
		long pixeles = (long) lector.getWidth(0) * lector.getHeight(0);
		if (pixeles > this.maximoPixeles) {
			throw new AficheInvalidoException("Esa imagen es demasiado grande: "
					+ lector.getWidth(0) + "×" + lector.getHeight(0) + " píxeles");
		}
		return lector.read(0);
	}

	/**
	 * La orientación que declara el EXIF, o 1 —"derecha"— si no la declara. Solo la traen los
	 * JPEG; un PNG o un WebP no tienen dónde ponerla.
	 *
	 * <p>Cualquier problema leyéndola devuelve 1 y sigue: un EXIF raro no puede ser el motivo de
	 * que un afiche no se pueda subir. Lo peor que pasa es que esa foto quede acostada, que es
	 * exactamente lo que pasaba antes de leerla.</p>
	 */
	private static int orientacionExif(byte[] original, String formato) {
		if (!formato.toLowerCase(Locale.ROOT).startsWith("jp")) {
			return 1;
		}
		try (ImageInputStream entrada = new MemoryCacheImageInputStream(new ByteArrayInputStream(original))) {
			List<JPEGSegment> segmentos = JPEGSegmentUtil.readSegments(entrada, JPEG.APP1, "Exif");
			if (segmentos.isEmpty()) {
				return 1;
			}
			InputStream datos = segmentos.get(0).data();
			datos.skip(1); // el byte de relleno que sigue al identificador "Exif\0"
			Directory exif = new TIFFReader().read(new MemoryCacheImageInputStream(datos));
			Entry orientacion = exif == null ? null : exif.getEntryById(TIFF.TAG_ORIENTATION);
			return orientacion == null ? 1 : ((Number) orientacion.getValue()).intValue();
		}
		catch (Exception ex) {
			log.debug("No se pudo leer la orientación EXIF del afiche; se asume derecha", ex);
			return 1;
		}
	}

	/**
	 * Las ocho orientaciones del EXIF, que son cuatro rotaciones y sus espejos. Las matrices van
	 * escritas como transformación de origen a destino: {@code (x, y)} de la imagen tal como está
	 * guardada, a {@code (x, y)} de la imagen como hay que verla.
	 */
	private static BufferedImage orientar(BufferedImage imagen, int orientacion) {
		if (orientacion <= 1 || orientacion > 8) {
			return imagen;
		}
		int ancho = imagen.getWidth();
		int alto = imagen.getHeight();
		AffineTransform giro = switch (orientacion) {
			case 2 -> new AffineTransform(-1, 0, 0, 1, ancho, 0);      // espejo horizontal
			case 3 -> new AffineTransform(-1, 0, 0, -1, ancho, alto);  // 180°
			case 4 -> new AffineTransform(1, 0, 0, -1, 0, alto);       // espejo vertical
			case 5 -> new AffineTransform(0, 1, 1, 0, 0, 0);           // traspuesta
			case 6 -> new AffineTransform(0, 1, -1, 0, alto, 0);       // 90° a la derecha
			case 7 -> new AffineTransform(0, -1, -1, 0, alto, ancho);  // antitraspuesta
			default -> new AffineTransform(0, -1, 1, 0, 0, ancho);     // 90° a la izquierda
		};
		boolean acostada = orientacion >= 5;
		BufferedImage destino = lienzo(acostada ? alto : ancho, acostada ? ancho : alto);
		Graphics2D pincel = destino.createGraphics();
		try {
			pincel.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			pincel.drawImage(imagen, giro, null);
		}
		finally {
			pincel.dispose();
		}
		return destino;
	}

	/**
	 * Encaja la imagen en la caja sin recortar ni deformar. Baja a la mitad mientras le sobre
	 * más de un factor dos: un solo salto bicúbico desde un original de imprenta deja escalones
	 * en las letras, y la mitad de los afiches del teatro independiente tienen el título impreso
	 * adentro de la imagen (D79).
	 */
	private static BufferedImage encajar(BufferedImage imagen) {
		double escala = Math.min(ANCHO_MAXIMO / (double) imagen.getWidth(),
				ALTO_MAXIMO / (double) imagen.getHeight());
		if (escala >= 1) {
			return imagen.getType() == BufferedImage.TYPE_INT_RGB ? imagen
					: dibujar(imagen, imagen.getWidth(), imagen.getHeight());
		}
		int anchoFinal = Math.max(1, (int) Math.round(imagen.getWidth() * escala));
		int altoFinal = Math.max(1, (int) Math.round(imagen.getHeight() * escala));
		BufferedImage actual = imagen;
		while (actual.getWidth() / 2 >= anchoFinal && actual.getHeight() / 2 >= altoFinal) {
			actual = dibujar(actual, actual.getWidth() / 2, actual.getHeight() / 2);
		}
		return dibujar(actual, anchoFinal, altoFinal);
	}

	private static BufferedImage dibujar(BufferedImage imagen, int ancho, int alto) {
		BufferedImage destino = lienzo(ancho, alto);
		Graphics2D pincel = destino.createGraphics();
		try {
			pincel.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			pincel.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			pincel.drawImage(imagen, 0, 0, ancho, alto, null);
		}
		finally {
			pincel.dispose();
		}
		return destino;
	}

	/**
	 * Siempre RGB con fondo blanco: el JPEG no tiene transparencia, y un PNG o un WebP con alfa
	 * dibujado sobre negro sale con un halo sucio alrededor del recorte.
	 */
	private static BufferedImage lienzo(int ancho, int alto) {
		BufferedImage destino = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		Graphics2D pincel = destino.createGraphics();
		try {
			pincel.setColor(Color.WHITE);
			pincel.fillRect(0, 0, ancho, alto);
		}
		finally {
			pincel.dispose();
		}
		return destino;
	}

	/**
	 * A JPEG con calidad explícita. El archivo sale sin EXIF ni perfiles: lo que se guarda es la
	 * imagen ya orientada, así que la orientación viajando aparte sería un dato que contradice al
	 * píxel, y la ubicación de la foto no tiene nada que hacer en el catálogo.
	 */
	private static byte[] codificar(BufferedImage imagen) throws IOException {
		ImageWriter escritor = ImageIO.getImageWritersByFormatName("jpeg").next();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ImageOutputStream salida = ImageIO.createImageOutputStream(bytes)) {
			escritor.setOutput(salida);
			ImageWriteParam parametros = escritor.getDefaultWriteParam();
			parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			parametros.setCompressionQuality(CALIDAD);
			escritor.write(null, new IIOImage(imagen, null, null), parametros);
		}
		finally {
			escritor.dispose();
		}
		return bytes.toByteArray();
	}
}
