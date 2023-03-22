package de.stf.hue;

import com.philips.lighting.model.PHLightState;

public class State {
	float x;
	float y;
	boolean on;
	int brightness;

	State(PHLightState org) {
		this.x = org.getX();
		this.y = org.getY();
		this.on = org.isOn();
		this.brightness = org.getBrightness();
	}

	@Override
	public String toString() {
		return "State {" +
			"brightness="+ brightness +
			", x=" + x +
			", y=" + y +
			", on=" + on +
			'}';
	}
}
