package de.stf.hue;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HueListener implements PHSDKListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(HueListener.class);
	public static final String BRIDGE_ID = "PILLEPALLE_BRIDGE";
	private PHHueSDK hueSdk;
	private PHAccessPoint accessPoint;

	HueListener(final PHHueSDK hue, final PHAccessPoint accessPoint) {
		this.hueSdk = hue;
		this.accessPoint = accessPoint;
	}

	@Override
	public void onCacheUpdated(List<Integer> arg0, PHBridge bridge) {
		LOGGER.trace("Cache updated: {}", bridge.toString());
	}

	@Override
	public void onBridgeConnected(PHBridge bridge, String username) {
		hueSdk.setSelectedBridge(bridge);
		hueSdk.enableHeartbeat(bridge, PHHueSDK.HB_INTERVAL);
		String lastIpAddress =
			bridge.getResourceCache().getBridgeConfiguration().getIpAddress();
		HueProperties.storeUsername(username);
		HueProperties.storeLastIPAddress(lastIpAddress);
		HueProperties.saveProperties();
		LOGGER.info("Connected apiKey={}, ip={}", username, lastIpAddress);
		accessPoint.setBridgeId(BRIDGE_ID);
	}

	@Override
	public void onAuthenticationRequired(PHAccessPoint accessPoint) {
		LOGGER.warn("Press the button at accessPoint {}", accessPoint.getBridgeId());
	}

	@Override
	public void onAccessPointsFound(List<PHAccessPoint> accessPointsList) {
		accessPointsList.forEach(ap ->
			LOGGER.info("AccessPoint {}", accessPoint.getBridgeId()));
	}

	@Override
	public void onError(int code, final String message) {
		if (code == PHHueError.BRIDGE_NOT_RESPONDING) {
			LOGGER.error("Bridge is not responding: {}", message);
		} else if (code == PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED) {
			LOGGER.error("Pushlink button not pressed: {}", message);
		} else if (code == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
			LOGGER.error("Pushlink auth failed: {}", message);
		} else if (code == PHMessageType.BRIDGE_NOT_FOUND) {
			LOGGER.error("Bridge not found: {}", message);
		} else
			LOGGER.error("Bridge error code={}, message={}", code, message);
	}

	@Override
	public void onConnectionResumed(PHBridge bridge) {
		LOGGER.info("Connection resumed: {}", bridge.toString());
	}

	@Override
	public void onConnectionLost(PHAccessPoint ap) {
		LOGGER.warn("Connection lost: {}", ap.toString());
	}

	@Override
	public void onParsingErrors(List<PHHueParsingError> parsingErrorsList) {
		for (PHHueParsingError parsingError : parsingErrorsList) {
			LOGGER.error("ParsingError: {}", parsingError.getMessage());
		}
	}
}
