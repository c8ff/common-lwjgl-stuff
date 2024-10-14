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

/**
 * Represents a playable and stoppable sound.
 * @author C8FF
 */
public interface ISound {
	/**
	 * Deletes this sound permanently.
	 * @see #isDeleted()
	 */
	void delete();

	/**
	 * Returns true if the sound was deleted.
	 */
	boolean isDeleted();

	/**
	 * If playing, then the sound is stopped, and then played again. If stopped, it just gets played.
	 * @see #play()
	 */
	void forcePlay();

	/**
	 * Starts the playback of the sound.
	 * No effect if the sound is already playing.
	 * @see #forcePlay()
	 */
	void play();

	/**
	 * Returns true if the sound is currently playing.
	 */
	boolean isPlaying();

	/**
	 * Pauses the sound's playback.
	 * No effect if the sound is already paused.
	 * @see #resume()
	 */
	void pause();

	/**
	 * Returns true if the sound is paused.
	 */
	boolean isPaused();

	/**
	 * Resumes the sound's playback.
	 * No effect if the sound is not paused.
	 * @see #pause()
	 */
	void resume();

	/**
	 * Stops the playback of the sound. The next play will be at the start.
	 * No effect if the sound is already stopped.
	 */
	void stop();

	/**
	 * Sets the gain (a.k.a. volume) of the sound.
	 * @param value The gain to set.
	 */
	void setGain(float value);

	/**
	 * Returns the gain.
	 * @return The gain of the sound.
	 */
	float getGain();

	/**
	 * Sets the pitch of the sound.
	 * @param value The pitch to set.
	 */
	void setPitch(float value);

	/**
	 * Returns the pitch.
	 * @return The pitch of the sound.
	 */
	float getPitch();

	/**
	 * Sets if the sound should loop when it ends.
	 * @param looping True if the sound should loop.
	 */
	void setLooping(boolean looping);

	/**
	 * Returns if the sound is looping.
	 * @return True if the sound loops back when the sound ends.
	 */
	boolean isLooping();
}
