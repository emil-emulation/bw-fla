/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.workflows.catalogdata;

import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;

public class ObjectEvaluationDescription extends ObjectEnvironmentDescription 
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9117415344270769129L;

	public ObjectEvaluationDescription(SystemEnvironmentHelper envHelper,
			String envId, String objId, String objRef) {
		super(envHelper, envId, objId, objRef);
		this.setDescriptionType(DescriptionTypes.TYPE.EVALUATION);
	}
	
	public static ObjectEvaluationDescription fromString(String json)
	{
		return (ObjectEvaluationDescription) DescriptionSerializer.fromString(json, ObjectEvaluationDescription.class);
	}
	
	private boolean installation_required;
	private boolean installation_sucessful;
	private boolean installation_extra;
	private boolean installation_reboot;
	private String notes_installation;
	
	// animation
	private boolean fullscreen_animation_required;
	private boolean fullscreen_animation_emulation;

	private boolean high_framerate_animation_required;
	private boolean high_framerate_animation_emulation;

	private boolean moving_objects_required;
	private boolean moving_objects_emulation;

	private boolean mutating_objects_required;
	private boolean mutating_objects_emulation;
	
	private String notes_animation;

	// audio
	private boolean audio_loops_required;
	private boolean audio_loops_emulation;

	private boolean sound_effects_required;
	private boolean sound_effects_emulation;
	
	private String notes_audio;

	// interactivity
	private boolean keyboard_input_required;
	private boolean keyboard_input_emulation;

	private boolean mouse_input_required;
	private boolean mouse_input_emulation;

	private boolean low_latency_required;
	private boolean low_latency_emulation;
	
	private String notes_interactivity;
	
	// Overall Performance
	private int overall_performance;
	private String notes_overall;

	public boolean isFullscreen_animation_required() {
		return fullscreen_animation_required;
	}

	public String getNotes_animation() {
		return notes_animation;
	}

	public String getNotes_audio() {
		return notes_audio;
	}

	public String getNotes_interactivity() {
		return notes_interactivity;
	}

	public String getNotes_installation() {
		return notes_installation;
	}

	public void setNotes_installation(String n)
	{
		notes_installation = n;
	}

	public void setNotes_animation(String notes_animation) {
		this.notes_animation = notes_animation;
	}

	public void setNotes_audio(String notes_audio) {
		this.notes_audio = notes_audio;
	}

	public int getOverall_performance() {
		return overall_performance;
	}

	public String getNotes_overall() {
		return notes_overall;
	}

	public String getAccess()
	{
		return access;
	}
	
	public void setOverall_performance(int overall_performance) {
		this.overall_performance = overall_performance;
	}

	public void setNotes_overall(String notes_overall) {
		this.notes_overall = notes_overall;
	}

	public void setNotes_interactivity(String notes_interactivity) {
		this.notes_interactivity = notes_interactivity;
	}

	public void setFullscreen_animation_required(
			boolean fullscreen_animation_required) {
		this.fullscreen_animation_required = fullscreen_animation_required;
	}

	public boolean isFullscreen_animation_emulation() {
		return fullscreen_animation_emulation;
	}

	public void setFullscreen_animation_emulation(
			boolean fullscreen_animation_emulation) {
		this.fullscreen_animation_emulation = fullscreen_animation_emulation;
	}

	public boolean isHigh_framerate_animation_required() {
		return high_framerate_animation_required;
	}

	public void setHigh_framerate_animation_required(
			boolean high_framerate_animation_required) {
		this.high_framerate_animation_required = high_framerate_animation_required;
	}

	public boolean isHigh_framerate_animation_emulation() {
		return high_framerate_animation_emulation;
	}

	public void setHigh_framerate_animation_emulation(
			boolean high_framerate_animation_emulation) {
		this.high_framerate_animation_emulation = high_framerate_animation_emulation;
	}

	public boolean isMoving_objects_required() {
		return moving_objects_required;
	}

	public void setMoving_objects_required(boolean moving_objects_required) {
		this.moving_objects_required = moving_objects_required;
	}

	public boolean isMoving_objects_emulation() {
		return moving_objects_emulation;
	}

	public void setMoving_objects_emulation(boolean moving_objects_emulation) {
		this.moving_objects_emulation = moving_objects_emulation;
	}

	public boolean isMutating_objects_required() {
		return mutating_objects_required;
	}

	public void setMutating_objects_required(boolean mutating_objects_required) {
		this.mutating_objects_required = mutating_objects_required;
	}

	public boolean isMutating_objects_emulation() {
		return mutating_objects_emulation;
	}

	public void setMutating_objects_emulation(boolean mutating_objects_emulation) {
		this.mutating_objects_emulation = mutating_objects_emulation;
	}

	public boolean isAudio_loops_required() {
		return audio_loops_required;
	}

	public void setAudio_loops_required(boolean audio_loops_required) {
		this.audio_loops_required = audio_loops_required;
	}

	public boolean isAudio_loops_emulation() {
		return audio_loops_emulation;
	}

	public void setAudio_loops_emulation(boolean audio_loops_emulation) {
		this.audio_loops_emulation = audio_loops_emulation;
	}

	public boolean isSound_effects_required() {
		return sound_effects_required;
	}

	public void setSound_effects_required(boolean sound_effects_required) {
		this.sound_effects_required = sound_effects_required;
	}

	public boolean isSound_effects_emulation() {
		return sound_effects_emulation;
	}

	public void setSound_effects_emulation(boolean sound_effects_emulation) {
		this.sound_effects_emulation = sound_effects_emulation;
	}

	public boolean isKeyboard_input_required() {
		return keyboard_input_required;
	}

	public void setKeyboard_input_required(boolean keyboard_input_required) {
		this.keyboard_input_required = keyboard_input_required;
	}

	public boolean isKeyboard_input_emulation() {
		return keyboard_input_emulation;
	}

	public void setKeyboard_input_emulation(boolean keyboard_input_emulation) {
		this.keyboard_input_emulation = keyboard_input_emulation;
	}

	public boolean isMouse_input_required() {
		return mouse_input_required;
	}

	public void setMouse_input_required(boolean mouse_input_required) {
		this.mouse_input_required = mouse_input_required;
	}

	public boolean isMouse_input_emulation() {
		return mouse_input_emulation;
	}

	public void setMouse_input_emulation(boolean mouse_input_emulation) {
		this.mouse_input_emulation = mouse_input_emulation;
	}

	public boolean isLow_latency_required() {
		return low_latency_required;
	}

	public void setLow_latency_required(boolean low_latency_required) {
		this.low_latency_required = low_latency_required;
	}

	public boolean isLow_latency_emulation() {
		return low_latency_emulation;
	}

	public void setLow_latency_emulation(boolean low_latency_emulation) {
		this.low_latency_emulation = low_latency_emulation;
	}
	
	public void setAccess(String s)
	{
		access = s;
	}

	public boolean isInstallation_required() {
		return installation_required;
	}

	public void setInstallation_required(boolean installation_required) {
		this.installation_required = installation_required;
	}

	public boolean isInstallation_sucessful() {
		return installation_sucessful;
	}

	public void setInstallation_sucessful(boolean installation_sucessful) {
		this.installation_sucessful = installation_sucessful;
	}

	public boolean isInstallation_extra() {
		return installation_extra;
	}

	public void setInstallation_extra(boolean installation_extra) {
		this.installation_extra = installation_extra;
	}

	public boolean isInstallation_reboot() {
		return installation_reboot;
	}

	public void setInstallation_reboot(boolean installation_reboot) {
		this.installation_reboot = installation_reboot;
	}
}
