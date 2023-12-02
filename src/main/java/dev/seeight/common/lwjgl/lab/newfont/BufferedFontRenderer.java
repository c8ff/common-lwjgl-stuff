package dev.seeight.common.lwjgl.lab.newfont;

import dev.seeight.common.lwjgl.font.json.CharacterData;
import dev.seeight.renderer.renderer.gl.GLUtil;
import dev.seeight.renderer.renderer.gl.OpenGLRenderer2;
import dev.seeight.renderer.renderer.gl.components.GLProgram;
import dev.seeight.renderer.renderer.gl.components.GLVertexArrayObject;
import dev.seeight.renderer.renderer.gl.components.GLVertexBufferObject;
import dev.seeight.renderer.renderer.gl.exception.UniformNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

/**
 * Builds triangles and uploads them into the GPU using array buffer objects from OpenGL.
 * Uses a program, vertex buffer and array buffer. The main reason this approach is faster
 * is that this approach doesn't require a lot of calculations compared to the slower implementation
 * {@link dev.seeight.common.lwjgl.font.FontRenderer}.<p>
 * This is a drop-in replacement for the previous mentioned class.
 *
 * @author C8FF
 */
public class BufferedFontRenderer implements IFontRenderer {
	/**
	 * This buffer contains all the triangles that are going to be rendered.
	 */
	private final FloatBuffer buffer;
	/**
	 * The count of how many floats have been appended to the {@link #buffer}.
	 */
	private int length;
	/**
	 * The count of how many vertices are going to be rendered.
	 */
	private int vertices;
	/**
	 * The scale of the rendering string in the X axis.
	 */
	private float scaleX = 1;
	/**
	 * The scale of the rendering string in the Y axis.
	 */
	private float scaleY = 1;

	/**
	 * The program that allows the rendering of the characters.<p>
	 * This shader uses X, Y, U, V coordinates for each vertex.
	 */
	private final GLProgram program;
	/**
	 * The object which the data is uploaded to.
	 */
	private final GLVertexBufferObject vbo;
	/**
	 * Determines what is on the {@link #vbo}.
	 */
	private final GLVertexArrayObject vao;

	/**
	 * Used to restore the renderer's GL objects.
	 */
	private final OpenGLRenderer2 renderer;

	/**
	 * Constructs a font renderer.
	 *
	 * @param renderer An OpenGLRenderer2 instance. This is not used to render
	 *                 the characters, but to restore the {@code program}, {@code vbo}, and {@code vao}.
	 */
	// AMAZING !!!
	public BufferedFontRenderer(OpenGLRenderer2 renderer) {
		this.renderer = renderer;

		// Capacity for (6144 / 3 / 4 = 512) characters. Why would you need more.
		this.buffer = BufferUtils.createFloatBuffer(6144);

		// Create shader
		this.program = new GLProgram();
		this.program.delete();
		this.program.init(this.getVertexSource(), this.getFragmentSource());
		this.renderer.useProgram(this.program);

		// Create vbo.
		this.vbo = new GLVertexBufferObject();
		this.vbo.init(true);
		// Allocate the maximum capacity.
		GLUtil.arrayBufferData(this.buffer.capacity() * Float.BYTES, GL15.GL_STATIC_DRAW);

		// Define what's on the array buffer.
		this.vao = new GLVertexArrayObject.Builder().floatAttribute(4).build();
		GL30.glEnableVertexAttribArray(0);

		// Restore renderer's objects.
		this.renderer.useDefaultProgram();
		this.renderer.useDefaultVao();
		this.renderer.useDefaultVbo();
	}

	@Override
	public float drawString(IFont font, char @NotNull [] characters, float x, float y, int start, int end) throws IndexOutOfBoundsException {
		IFontRenderer.assertIndices(characters.length, start, end);

		// Build triangles.
		length = 0;
		vertices = 0;
		buffer.clear();
		float startX = x;
		float maxX = x;
		for (int i = start; i < end; i++) {
			int codePoint = characters[i];
			if (isNewLine(codePoint)) {
				x = startX;
				y += getNewLineHeight(font);
				continue;
			}

			// These used to render the characters directly, but now they append
			// triangles into the buffer.
			CharacterData data = font.getCharacterData(codePoint);
			if (data != null) {
				drawChar(font, data, x, y);
			} else {
				drawInvalidChar(font, codePoint, x, y);
			}

			x += getCharacterWidth(font, data, codePoint);
		}

		// Adjust the return value
		if (maxX < x) {
			maxX = x;
		}

		// Bind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, font.getTexture().getId());

		// Start rendering
		this.renderer.useProgram(this.program);
		this.vbo.bind();
		this.vao.bind();

		// Upload characters
		// The buffer is limited to prevent uploading more than needed.
		this.buffer.position(0);
		GLUtil.arrayBufferSubData(0, this.buffer.limit(this.length));

		// Upload uniforms to the shader.
		try {
			this.renderer.uploadColor();
			this.renderer.uploadProjectionAndView();
		} catch (UniformNotFoundException ignored) {
		}

		// Draw all necessary vertices.
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.vertices);

		// Restore the renderer's objects.
		this.renderer.useDefaultProgram();
		this.renderer.useDefaultVao();
		this.renderer.useDefaultVbo();

		return maxX;
	}

	@Override
	public void setScaleX(float scaleX) {
		this.scaleX = scaleX;
	}

	@Override
	public void setScaleY(float scaleY) {
		this.scaleY = scaleY;
	}

	@Override
	public float getScaleX() {
		return this.scaleX;
	}

	@Override
	public float getScaleY() {
		return this.scaleY;
	}

	@Override
	public void drawChar(IFont font, @NotNull CharacterData data, float _x, float _y) {
		float x = _x + data.renderOriginX * this.getScaleX();
		float y = _y + data.renderOriginY * this.getScaleY();
		float x2 = x + data.renderWidth * this.getScaleX();
		float y2 = y + data.renderHeight * this.getScaleY();

		float u = (float) data.u();
		float v = (float) data.v();
		float u2 = (float) data.u2();
		float v2 = (float) data.v2();

		// x, y                 x2, y
		// x----------|---------x
		// |                    |
		// |                    |
		// |                    |
		// |                    |
		// x----------|---------x
		// x, y2                x2, y2

		// First triangle
		this.buffer.put(x).put(y);
		this.buffer.put(u).put(v);

		this.buffer.put(x2).put(y);
		this.buffer.put(u2).put(v);

		this.buffer.put(x2).put(y2);
		this.buffer.put(u2).put(v2);

		// Second triangle
		this.buffer.put(x2).put(y2);
		this.buffer.put(u2).put(v2);

		this.buffer.put(x).put(y2);
		this.buffer.put(u).put(v2);

		this.buffer.put(x).put(y);
		this.buffer.put(u).put(v);

		// Count the used space and how many vertices were written.
		this.length += 3 * 2 * 4;
		this.vertices += 6;
	}

	@Override
	public void drawInvalidChar(IFont font, int codepoint, float x, float y) {

	}

	protected String getVertexSource() {
		return """
				#version 430
				
				layout (location = 0) in vec4 vertex;

				uniform mat4 projection;
				uniform mat4 view;
				
				out vec2 fragCoords;
                
				void main() {
					gl_Position = projection * view * vec4(vertex.xy, 0.0, 1.0);
					fragCoords = vertex.zw;
				}
				""";
	}

	protected String getFragmentSource() {
		return """
				#version 430

				uniform sampler2D t;

				uniform vec4 shapeColor;
				in vec2 fragCoords;
				out vec4 color;
                
				void main() {
					color = texture2D(t, fragCoords) * shapeColor;
				}
				""";
	}
}
