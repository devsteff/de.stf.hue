package de.stf.hue;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.*;
import de.stf.hue.config.HueProperties;
import de.stf.hue.model.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

public class HueRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(HueRunner.class);
	protected static PHHueSDK HUE_SDK;
	protected static PHAccessPoint accessPoint;
	private static HueListener phsdkListener;

	public HueRunner() {
		if (HUE_SDK == null) {
			LOGGER.debug("Init 'HueRunner'");
			HueProperties.loadProperties();
			HUE_SDK = PHHueSDK.getInstance();
			HUE_SDK.setAppName("de.stf.hue");
			if (!connectToLastKnownAccessPoint()) {
				LOGGER.error("Unable to connect to access point");
				System.exit(1);
			}
		}
	}

	/**
	 Connect to the last known access point. This method is triggered by the Connect to Bridge
	 button, but it can equally be used to automatically connect to a bridge.
	 */
	private boolean connectToLastKnownAccessPoint() {
		String username = HueProperties.getUsername();
		String lastIpAddress = HueProperties.getLastConnectedIP();
		if (username == null || lastIpAddress == null) {
			return false;
		}
		accessPoint = new PHAccessPoint();
		accessPoint.setIpAddress(lastIpAddress);
		accessPoint.setUsername(username);
		phsdkListener = new HueListener(HUE_SDK, accessPoint);
		HUE_SDK.getNotificationManager().registerSDKListener(phsdkListener);
		HUE_SDK.connect(accessPoint);
		return true;
	}

	@SuppressWarnings("all")
	public static void main(String[] argv) {
		try {
			new HueRunner(); // initialize
			do {
				sleep(50);
			} while (accessPoint.getBridgeId() == null);
			PHBridge bridge = HUE_SDK.getSelectedBridge();
			LOGGER.debug("Bridge: {} {}", accessPoint.getBridgeId(), bridge);
			List<PHLight> allLights = bridge.getResourceCache().getAllLights();
			//remember states of all lights
			Map<String, State> lightStates = rememberLightsAndStates(allLights);
			//animation sequenz
            flicker(bridge, allLights, HueProperties.getLightFilter());
            //reset all lights to original state
			resetAllLights(bridge, allLights, lightStates);
			Thread.sleep(2000); //wait some time
		} catch (InterruptedException ignored) {
		} finally {
			final String bridgeId = accessPoint.getBridgeId();
			HUE_SDK.getAllBridges().forEach(bridge ->
				LOGGER.debug("Terminating Bridge {} {}", bridgeId, bridge)
			);
			HUE_SDK.disableAllHeartbeat();
			HUE_SDK.disconnect(HUE_SDK.getSelectedBridge());
			HUE_SDK.destroySDK();
		}
	}

	private static Map<String, State> rememberLightsAndStates(List<PHLight> allLights) {
		Map<String, State> lightStates = new HashMap<>(allLights.size());
		LOGGER.debug("List all known lights named '*{}*' and remember current state",
			HueProperties.getLightFilter());
		allLights
			.stream()
			.filter(light -> light.getName().contains(HueProperties.getLightFilter()))
			.forEach(light -> {
				final PHLightState lastKnownLightState = light.getLastKnownLightState();
				lightStates.put(light.getUniqueId(), new State(lastKnownLightState));
				LOGGER.debug("Light {} -{} is {}\n  {}", light.getName(), light.getUniqueId(),
					(lastKnownLightState.isOn() ? "AN" : "AUS"),
					lightStates.get(light.getUniqueId()));
				safeSleep(50);
		});
		return lightStates;
	}

	private static void safeSleep(long millis) {
		try { sleep(millis); } catch (InterruptedException ignored) {}
	}

	private static void resetAllLights(
		PHBridge bridge, List<PHLight> allLights,
		Map<String, State> lightStates
	) {
		allLights
			.stream()
			.filter(light -> light.getName().contains(HueProperties.getLightFilter()))
			.forEach(light -> {
				bridge.updateLightState(light, lightStates.get(light.getUniqueId()).asPHLightState());
				LOGGER.debug("Light reset {} -{}\n  {}", light.getName(), light.getUniqueId(),
					lightStates.get(light.getUniqueId()));
				safeSleep(50);
			});
	}

    private static void flicker(PHBridge bridge, List<PHLight> allLights, String lightsNameFilter)
        throws InterruptedException {
        for (int s = 0; s < 5; s++) { //Loops
            final int[] l = { s };
            LOGGER.debug("Loop {} for lights containing '{}", l[0], lightsNameFilter);
            allLights
                .stream()
                .filter(light -> light.getName().contains(lightsNameFilter))
                .forEach(light -> {
                    LOGGER.debug("  Light {}", light.getName());
                    final PHLightState lightState = new PHLightState();
                    lightState.setOn(true);
                    float[] xy;
                    if (l[0] % 2 > 0)
                        xy = PHUtilities.calculateXYFromRGB(255, 0, 0, "LCT001");
                    else
                        xy = PHUtilities.calculateXYFromRGB(255, 255, 255, "LCT001");
                    lightState.setX(xy[0]);
                    lightState.setY(xy[1]);
                    // If no bridge response is required then use this simple form
                    bridge.updateLightState(light, lightState);
                });
            sleep(1000);
        }
    }
}
