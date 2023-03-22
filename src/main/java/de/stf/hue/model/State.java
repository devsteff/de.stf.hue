package de.stf.hue.model;

import com.philips.lighting.model.PHLightState;

public class State {
	public float x;
	public float y;
	public boolean on;
	public int brightness;

	public State(PHLightState org) {
		this.x = org.getX();
		this.y = org.getY();
		this.on = org.isOn();
		this.brightness = org.getBrightness();
	}

	public PHLightState asPHLightState() {
		final PHLightState phls = new PHLightState();
		phls.setOn(on);
		phls.setX(x);
		phls.setY(y);
		phls.setBrightness(brightness);
		return phls;
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
