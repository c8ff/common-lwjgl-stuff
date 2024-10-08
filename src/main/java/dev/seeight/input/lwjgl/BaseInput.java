package dev.seeight.input.lwjgl;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to represent input methods. For example, a keyboard or mouse.
 * @author c8ff
 */
public abstract class BaseInput {
	/**
	 * A list of the buttons to be updated.
	 */
	private final List<Button> buttons = new ArrayList<>();

	/**
	 * Creates a {@link Button} instance and adds it into the list of buttons to update.
	 * @param code The codes that will represent the button to be created.
	 * @return The resulting button.
	 */
	protected Button registerKey(int... code) {
		Button k = new Button(code);
		buttons.add(k);
		return k;
	}

	/**
	 * Updates the state of the {@link #buttons}.
	 * This method should only be called on a GLFW key callback or a mouse button callback.
	 */
	public void onInputEvent(int button, int action, int mods) {
		for (Button b : buttons) {
			if (b.isCode(button)) {
				b.press = b.down = action != GLFW.GLFW_RELEASE;
			}
		}
	}

	/**
	 * Un-presses the buttons that are 'pressed'.
	 */
	public void onFrameEnd() {
		for (Button button : buttons) button.press = false;
	}

	/**
	 * Represents a button with possibly many codes.
	 * @author c8ff
	 */
	public static class Button {
		/**
		 * The codes that this button represents.
		 */
		private final int[] codes;
		/**
		 * True if the button is being held.
		 */
		private boolean down;
		/**
		 * True for one frame (or before {@link #onFrameEnd()} is called) if the button is being held.
		 */
		private boolean press;

		/**
		 * Constructs a {@link Button} instance.
		 * @param codes The codes that this button represents.
		 */
		public Button(int... codes) {
			this.codes = codes;
		}

		/**
		 * Checks if the provided code matches with this button's codes.
		 * @param code The button code to check.
		 * @return True if the {@code code} matches with any of the elements of {@link #codes}.
		 */
		public boolean isCode(int code) {
			for (int i : codes) if (i == code) return true;
			return false;
		}

		/**
		 * @return True if the button is being pressed.
		 * @see #press
		 */
		public boolean isPress() {
			return press;
		}

		/**
		 * @return True if the button is being held.
		 * @see	#down
		 */
		public boolean isDown() {
			return this.down;
		}

		/**
		 * @return A clone of {@link #codes}.
		 */
		public int[] getCodes() {
			return this.codes.clone();
		}
	}
}
