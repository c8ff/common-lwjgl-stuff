package dev.seeight.common.lwjgl.svg;

import dev.seeight.renderer.renderer.Texture;
import dev.seeight.renderer.renderer.gl.components.GLTexture;
import org.apache.commons.io.IOUtils;
import org.lwjgl.nanovg.NSVGImage;
import org.lwjgl.nanovg.NanoSVG;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SVGRasterContext implements AutoCloseable {
	protected long context;

	protected float dpi = 96.0F;
	protected String measure = "px";

	protected SVGRasterContext() {
		context = NanoSVG.nsvgCreateRasterizer();
		if (context == MemoryUtil.NULL) {
			throw new IllegalStateException("Failed to create SVG rasterizer.");
		}
	}

	public SVGRasterContext setDpi(float dpi) {
		assertNotDeleted();

		this.dpi = dpi;
		return this;
	}

	public void _svgImageFromBytes(byte[] bytes, Consumer<NSVGImage> consumer) throws NullPointerException {
		assertNotDeleted();

		NSVGImage s;

		try (MemoryStack stack = MemoryStack.stackPush()) {
			// Move bytes into byte buffer
			ByteBuffer data = stack.malloc(bytes.length + 1);
			data.put(bytes);
			data.put((byte) 0); // What the fuck
			data.clear();

			s = NanoSVG.nsvgParse(data, stack.ASCII(measure), this.dpi);
		}

		if (s == null) throw new NullPointerException();

		try {
			consumer.accept(s);
		} catch (Exception e) {
			NanoSVG.nsvgDelete(s);
			e.printStackTrace();
		}
	}

	public void raster(byte[] svgBytes, float scale, RasterConsumer consumer) {
		assertNotDeleted();

		_svgImageFromBytes(svgBytes, img -> {
			int width  = (int)(img.width() * scale);
			int height = (int)(img.height() * scale);

			int colorChannels = 4;
			// Allocate memory of the image.
			ByteBuffer imageData = MemoryUtil.memAlloc(width * height * colorChannels);
			// Raster the image.
			NanoSVG.nsvgRasterize(context, img, 0, 0, scale, imageData, width, height, width * colorChannels);
			// Call consumer with the image data and size.
			try {
				consumer.accept(imageData, width, height);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Free allocated data.
			MemoryUtil.memFree(imageData);
		});
	}

	public Texture glTexFromResource(Class<?> r, String name, float scale) throws IOException {
		InputStream stream = r.getResourceAsStream(name);
		if (stream == null) throw new FileNotFoundException("Resource '" + name + " not found.");
		return glTexFromStream(stream, scale);
	}

	public Texture glTexFromStream(InputStream stream, float scale) throws IOException {
		if (stream == null) throw new NullPointerException("stream cannot be null.");
		return glTexFromBytes(IOUtils.toByteArray(stream), scale);
	}


	public Texture glTexFromString(String svg, float scale) {
		return this.glTexFromBytes(svg.getBytes(StandardCharsets.UTF_8), scale);
	}

	public Texture glTexFromBytes(byte[] svgBytes, float scale) {
		assertNotDeleted();

		AtomicReference<Texture> raster = new AtomicReference<>();

		raster(svgBytes, scale, (imageData, width, height) -> {
			// Save currently bound texture.
			int b = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

			// Create new texture
			int texID = GL11.glGenTextures();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);

			// Apply texture parameters.
			this.applyNewTextureParameters();

			// Upload data.
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageData);

			// Create mip map of texture.
			this.genMipMap();

			// Restore previous bound texture
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, b);

			raster.set(new GLTexture(texID, width, height));
		});

		return raster.get();
	}

	protected void applyNewTextureParameters() {
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
	}

	protected void genMipMap() {
		// Generate mip map.
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
	}

	protected void assertNotDeleted() {
		if (isDeleted()) {
			throw new IllegalStateException();
		}
	}

	public void delete() {
		if (this.isDeleted()) return;

		NanoSVG.nsvgDeleteRasterizer(context);
		context = -1;
	}

	public boolean isDeleted() {
		return this.context == -1;
	}

	@Override
	public void close() {
		this.delete();
	}

	public static SVGRasterContext create() {
		return new SVGRasterContext();
	}

	public interface RasterConsumer {
		void accept(ByteBuffer imageData, int width, int height);
	}
}
