/*
 * common-lwjgl-stuff
 * Copyright (C) 2024 c8ff
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.seeight.common.lwjgl.sound;

import dev.seeight.common.lwjgl.util.IOUtil;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.libc.LibCStdlib;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * A class that represents a sound in OpenAL.
 *
 * @author C8FF
 */
public class OpenALSound implements ISound {
	private final int bufferId;
	private final int sourceId;

	private boolean playing = false;
	private boolean paused = false;
	private boolean looping;

	private boolean isDeleted = false;

	private float gain = 0.3f;
	private float pitch = 1;

	@Deprecated
	public OpenALSound(String path, boolean loop) {
		this(path, loop, true);
	}

	/**
	 * Creates an ogg sound
	 */
	@Deprecated
	public OpenALSound(String path, boolean loop, boolean jar) {
		if (!path.endsWith(".ogg")) {
			throw new UnsupportedOperationException("Unknown format. The file must be a vorbis file.");
		}

		int channels;
		int sampleRate;
		ShortBuffer rawAudioBuffer;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer channelsBuffer = stack.mallocInt(1);
			IntBuffer sampleRateBuffer = stack.mallocInt(1);

			if (jar) {
				try {
					rawAudioBuffer = STBVorbis.stb_vorbis_decode_memory(IOUtil.byteBufferFrom(OpenALSound.class, path), channelsBuffer, sampleRateBuffer);
				} catch (IOException e) {
					throw new RuntimeException("Couldn't read the specified sound file: " + path, e);
				}
			} else {
				rawAudioBuffer = STBVorbis.stb_vorbis_decode_filename(path, channelsBuffer, sampleRateBuffer);
			}

			if (rawAudioBuffer == null) {
				throw new RuntimeException("Couldn't load sound '" + path + "'.");
			}

			channels = channelsBuffer.get();
			sampleRate = sampleRateBuffer.get();
		}

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
		this.setLooping(loop);
		this.alSourcei(AL10.AL_POSITION, 0);

		this.setGain(this.gain);
		this.setPitch(this.pitch);

		LibCStdlib.free(rawAudioBuffer);
	}

	public OpenALSound(int channels, int sampleRate, ShortBuffer rawAudioBuffer) {
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
		this.alSourcei(AL10.AL_POSITION, 0);

		this.setGain(this.gain);
		this.setPitch(this.pitch);
	}

	private void alSourcei(int param, int value) {
		AL10.alSourcei(this.sourceId, param, value);
	}

	@Override
	public void delete() {
		if (!this.isDeleted) {
			AL10.alDeleteSources(this.sourceId);
			AL10.alDeleteBuffers(this.bufferId);

			this.isDeleted = true;
		}
	}

	@Override
	public void forcePlay() {
		if (isPlaying()) {
			stop();
		}

		play();
	}

	@Override
	public void play() {
		int state = AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_STATE);
		if (state == AL10.AL_STOPPED) {
			this.playing = false;
			AL10.alSourcei(this.sourceId, AL10.AL_POSITION, 0);
		}

		if (!this.isPlaying()) {
			AL10.alSourcePlay(this.sourceId);
			this.playing = true;
			this.paused = false;
		}
	}

	@Override
	public void pause() {
		if (this.playing && !this.paused) {
			AL10.alSourcePause(this.sourceId);
			this.paused = true;
		}
	}

	@Override
	public void resume() {
		if (this.playing && this.paused) {
			AL10.alSourcePlay(this.sourceId);
			this.paused = false;
		}
	}

	@Override
	public void stop() {
		if (this.playing) {
			AL10.alSourceStop(this.sourceId);
			this.playing = false;
			this.paused = false;
		}
	}

	@Override
	public boolean isPlaying() {
		int state = AL10.alGetSourcei(this.sourceId, AL10.AL_SOURCE_STATE);
		if (state == AL10.AL_STOPPED) {
			this.playing = false;
		}

		return this.playing;
	}

	@Override
	public boolean isPaused() {
		return this.paused;
	}

	@Override
	public boolean isDeleted() {
		return this.isDeleted;
	}

	@Override
	public void setGain(float value) {
		this.gain = value;
		AL10.alSourcef(this.sourceId, AL10.AL_GAIN, this.gain);
	}

	@Override
	public float getGain() {
		return this.gain;
	}

	@Override
	public void setPitch(float value) {
		this.pitch = value;
		AL10.alSourcef(this.sourceId, AL10.AL_PITCH, value);
	}

	@Override
	public float getPitch() {
		return this.pitch;
	}

	@Override
	public void setLooping(boolean looping) {
		this.looping = looping;
		alSourcei(AL10.AL_LOOPING, looping ? 1 : 0);
	}

	@Override
	public boolean isLooping() {
		return looping;
	}

	public static OpenALSound fromInputStream(InputStream stream) throws IOException {
		ShortBuffer rawAudioBuffer;

		OpenALSound f;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer channelsBuffer = stack.mallocInt(1);
			IntBuffer sampleRateBuffer = stack.mallocInt(1);

			rawAudioBuffer = STBVorbis.stb_vorbis_decode_memory(IOUtil.byteBufferFrom(stream), channelsBuffer, sampleRateBuffer);

			if (rawAudioBuffer == null) {
				throw new RuntimeException("Couldn't load sound from input stream." + stream);
			}

			f = new OpenALSound(channelsBuffer.get(), sampleRateBuffer.get(), rawAudioBuffer);
			LibCStdlib.free(rawAudioBuffer);
		}

		return f;
	}

	public static OpenALSound fromFile(String path) {
		ShortBuffer rawAudioBuffer;

		OpenALSound f;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer channelsBuffer = stack.mallocInt(1);
			IntBuffer sampleRateBuffer = stack.mallocInt(1);

			rawAudioBuffer = STBVorbis.stb_vorbis_decode_filename(path, channelsBuffer, sampleRateBuffer);

			if (rawAudioBuffer == null) {
				throw new RuntimeException("Couldn't load sound from file '" + path + "'.");
			}

			f = new OpenALSound(channelsBuffer.get(), sampleRateBuffer.get(), rawAudioBuffer);
			LibCStdlib.free(rawAudioBuffer);
		}

		return f;
	}
}
