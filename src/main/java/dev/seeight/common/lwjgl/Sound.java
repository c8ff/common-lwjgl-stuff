package dev.seeight.common.lwjgl;

import dev.seeight.common.lwjgl.util.IOUtil;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.libc.LibCStdlib;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * A class that represents a sound in OpenAL.
 *
 * @author C8FF
 */
public class Sound {
	private final int bufferId;
	private final int sourceId;

	private final String path;

	private boolean isPlaying = false;
	private boolean isPaused = false;

	private boolean isDeleted = false;

	private float gain = 0.3f;
	private float pitch = 1;

	public Sound(String path, boolean loop) {
		this(path, loop, true);
	}

	/**
	 * Creates an ogg sound
	 */
	public Sound(String path, boolean loop, boolean jar) {
		this.path = path;

		if (!path.endsWith(".ogg")) {
			throw new UnsupportedOperationException("Unknown format. The file must be a vorbis file.");
		}

		MemoryStack.stackPush();
		IntBuffer channelsBuffer = MemoryStack.stackMallocInt(1);
		MemoryStack.stackPush();
		IntBuffer sampleRateBuffer = MemoryStack.stackMallocInt(1);

		ShortBuffer rawAudioBuffer;
		if (jar) {
			try {
				rawAudioBuffer = STBVorbis.stb_vorbis_decode_memory(IOUtil.byteBufferFrom(Sound.class, path), channelsBuffer, sampleRateBuffer);
			} catch (IOException e) {
				throw new RuntimeException("Couldn't read the specified sound file: " + path);
			}
		} else {
			rawAudioBuffer = STBVorbis.stb_vorbis_decode_filename(path, channelsBuffer, sampleRateBuffer);
		}

		if (rawAudioBuffer == null) {
			MemoryStack.stackPop();
			MemoryStack.stackPop();
			throw new RuntimeException("Couldn't load sound '" + path + "'.");
		}

		int channels = channelsBuffer.get();
		int sampleRate = sampleRateBuffer.get();

		MemoryStack.stackPop();
		MemoryStack.stackPop();

		int format = -1;
		if (channels == 1) {
			format = AL10.AL_FORMAT_MONO16;
		} else if (channels == 2) {
			format = AL10.AL_FORMAT_STEREO16;
		}

		this.bufferId = AL10.alGenBuffers();
		AL10.alBufferData(this.bufferId, format, rawAudioBuffer, sampleRate);

		this.sourceId = AL10.alGenSources();

		this.alSourcei(AL10.AL_BUFFER, this.bufferId);
		this.alSourcei(AL10.AL_LOOPING, loop ? 1 : 0);
		this.alSourcei(AL10.AL_POSITION, 0);

		this.setGain(this.gain);
		this.setPitch(this.pitch);

		LibCStdlib.free(rawAudioBuffer);
	}

	private void alSourcei(int param, int value) {
		AL10.alSourcei(this.sourceId, param, value);
	}

	public void delete() {
		if (!this.isDeleted) {
			AL10.alDeleteSources(this.sourceId);
			AL10.alDeleteBuffers(this.bufferId);

			this.isDeleted = true;
		}
	}

	public void forcePlay() {
		if (isPlaying()) {
			stop();
		}

		play();
	}

	public void play() {
		int state = AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_STATE);
		if (state == AL10.AL_STOPPED) {
			this.isPlaying = false;
			AL10.alSourcei(this.sourceId, AL10.AL_POSITION, 0);
		}

		if (!this.isPlaying) {
			AL10.alSourcePlay(this.sourceId);
			this.isPlaying = true;
			this.isPaused = false;
		}
	}

	public void pause() {
		if (this.isPlaying && !this.isPaused) {
			AL10.alSourcePause(this.sourceId);
			this.isPaused = true;
		}
	}

	public void resume() {
		if (this.isPlaying && this.isPaused) {
			AL10.alSourcePlay(this.sourceId);
			this.isPaused = false;
		}
	}

	public void stop() {
		if (this.isPlaying) {
			AL10.alSourceStop(this.sourceId);
			this.isPlaying = false;
			this.isPaused = false;
		}
	}

	public String getPath() {
		return this.path;
	}

	public boolean isPlaying() {
		int state = AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_STATE);
		if (state == AL10.AL_STOPPED) {
			this.isPlaying = false;
		}

		return this.isPlaying;
	}

	public boolean isPaused() {
		return this.isPaused;
	}

	public boolean isDeleted() {
		return this.isDeleted;
	}

	public void setGain(float value) {
		this.gain = value;
		AL10.alSourcef(this.sourceId, AL10.AL_GAIN, this.gain);
	}

	public float getGain() {
		return this.gain;
	}

	public void setPitch(float value) {
		this.pitch = value;
		AL10.alSourcef(this.sourceId, AL10.AL_PITCH, value);
	}

	public float getPitch() {
		return this.pitch;
	}
}
