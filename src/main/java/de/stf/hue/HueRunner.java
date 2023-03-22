package de.stf.hue;

import com.philips.lighting.hue.sdk.*;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HueRunner {
    private static Logger LOGGER = LoggerFactory.getLogger(HueRunner.class);
    public static HueRunner HueRunner = new HueRunner();
    private static final int MAX_HUE=65535;
    private static PHHueSDK HUE;
    private static PHAccessPoint accessPoint;

    public HueRunner() {
        if(HUE==null) {
            LOGGER.debug("Init Worker");
            HueProperties.loadProperties();
            HUE = PHHueSDK.getInstance();
            HUE.setAppName("de.stf.hue");
            HUE.getNotificationManager().registerSDKListener(getHueListener());
            if (!this.connectToLastKnownAccessPoint()) {
                LOGGER.error("Unable to connect to access point");
                System.exit(1);
            }
        }
    }

    public static void main(String argv[]) {
        try {
            do { Thread.sleep(50); } while(accessPoint.getBridgeId()==null);
            PHBridge bridge = HUE.getSelectedBridge();
            LOGGER.debug("Bridge: {} {}", accessPoint.getBridgeId(), bridge);
            List<PHLight> allLights = bridge.getResourceCache().getAllLights();
            List<PHLightState> lightStates = new ArrayList<>(allLights.size());
            allLights.stream().forEach(light -> {
                PHLightState lastKnownLightState = light.getLastKnownLightState();
                lightStates.add(lastKnownLightState);
                PHLightState lightState = new PHLightState();
                lightState.setOn(true);
                bridge.updateLightState(light, lightState); // If no bridge response is required then use this simpler form.
                LOGGER.debug("Light {} is {}", light.getUniqueId(), lastKnownLightState.isOn()?"AN":"AUS");
            });
            Random rand = new Random();
            for(int s=0; s<9; s++) {
                final int[] l = { 0, s };
                LOGGER.debug("Loop {}", s);
                allLights.stream()
                    .forEach(light -> {
                        LOGGER.debug("  Lampe {}", l);
                        PHLightState lightState = new PHLightState();
                        float xy[] = null;
                        if(l[1]%2>0)
                            xy = PHUtilities.calculateXYFromRGB(255, 0, 0, "LCT001");
                        else
                            xy = PHUtilities.calculateXYFromRGB(255, 255, 255, "LCT001");
                        lightState.setX(xy[0]);
                        lightState.setY(xy[1]);
                        bridge.updateLightState(light, lightState); // If no bridge response is required then use this simpler form.
                    });
                Thread.sleep(2000);
            }
            // reset to original state
            final int[] l = { 0 };
            allLights.stream().forEach(light -> {
                PHLightState lightState = new PHLightState();
                lightState.setX(lightStates.get(l[0]).getX());
                lightState.setY(lightStates.get(l[0]).getY());
                l[0]+=1;
                bridge.updateLightState(light, lightState); // If no bridge response is required then use this simpler form.
                LOGGER.debug("Light reset {}", light.getUniqueId());
            });
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        } finally {
            final String bridgeId = accessPoint.getBridgeId();
            HUE.getAllBridges().stream().forEach(bridge ->
                LOGGER.debug("Terminating Bridge {} {}", bridgeId, bridge)
            );
            HUE.disableAllHeartbeat();
            HUE.disconnect(HUE.getSelectedBridge());
            HUE.destroySDK();
        }
    }

    /**
     * Connect to the last known access point.
     * This method is triggered by the Connect to Bridge button but it can equally be used to automatically connect to a bridge.
     *
     */
    private boolean connectToLastKnownAccessPoint() {
        String username = HueProperties.getUsername();
        String lastIpAddress =  HueProperties.getLastConnectedIP();
        if (username==null || lastIpAddress == null) {
             return false;
        }
        accessPoint = new PHAccessPoint();
        accessPoint.setIpAddress(lastIpAddress);
        accessPoint.setUsername(username);
        HUE.connect(accessPoint);
        return true;
    }

    public PHHueSDK getHUE() {
        return HUE;
    };

    private static PHSDKListener getHueListener() {
        return new PHSDKListener() {
            @Override
            public void onAccessPointsFound(List<PHAccessPoint> accessPointsList) {
                accessPointsList.stream().forEach(ap -> {
                    LOGGER.info("AccessPoint {}", accessPoint.getBridgeId());
                });
            }

            @Override
            public void onAuthenticationRequired(PHAccessPoint accessPoint) {
                LOGGER.warn("Press the button at accessPoint {}", accessPoint.getBridgeId());
            }

            @Override
            public void onBridgeConnected(PHBridge bridge, String username) {
                HUE.setSelectedBridge(bridge);
                HUE.enableHeartbeat(bridge, PHHueSDK.HB_INTERVAL);
                String lastIpAddress = bridge.getResourceCache().getBridgeConfiguration().getIpAddress();
                HueProperties.storeUsername(username);
                HueProperties.storeLastIPAddress(lastIpAddress);
                HueProperties.saveProperties();
                LOGGER.info("Connected apiKey={}, ip={}", username, lastIpAddress);
                accessPoint.setBridgeId("pillepalle");
            }

            @Override
            public void onCacheUpdated(List<Integer> arg0, PHBridge bridge) {
                LOGGER.debug("Cache updated: {}", bridge.toString());
            }

            @Override
            public void onConnectionLost(PHAccessPoint ap) {
                LOGGER.warn("Connection lost: {}", ap.toString());
            }

            @Override
            public void onConnectionResumed(PHBridge bridge) {
                LOGGER.info("Connection resumed: {}", bridge.toString());
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
            public void onParsingErrors(List<PHHueParsingError> parsingErrorsList) {
                for (PHHueParsingError parsingError : parsingErrorsList) {
                    LOGGER.error("ParsingError: {}", parsingError.getMessage());
                }
            }
        };
    }
}
