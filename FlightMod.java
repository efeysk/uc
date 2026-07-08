package com.example.flighthack;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

public class FlightMod implements ModInitializer {
    private static KeyBinding toggleKey;
    private static KeyBinding bypassKey;
    private static KeyBinding speedUpKey;
    private static KeyBinding speedDownKey;

    @Override
    public void onInitialize() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.flight.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "category.flight"
        ));

        bypassKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.flight.bypass",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.flight"
        ));

        speedUpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.flight.speedup",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            "category.flight"
        ));

        speedDownKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.flight.speeddown",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            "category.flight"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKey.wasPressed()) {
                FlightMixin.toggleFlight();
                if (client.player != null) {
                    client.player.sendMessage(new LiteralText(
                        "Flight: " + (FlightMixin.isFlightEnabled() ? "ON" : "OFF")
                    ), false);
                }
            }

            if (bypassKey.wasPressed()) {
                FlightMixin.setBypass(!FlightMixin.isBypassActive());
                if (client.player != null) {
                    client.player.sendMessage(new LiteralText(
                        "Bypass: " + (FlightMixin.isBypassActive() ? "ON" : "OFF")
                    ), false);
                }
            }

            if (speedUpKey.wasPressed()) {
                float current = FlightMixin.getFlySpeed();
                FlightMixin.setFlySpeed(current + 0.05F);
                if (client.player != null) {
                    client.player.sendMessage(new LiteralText(
                        "Fly Speed: " + String.format("%.2f", FlightMixin.getFlySpeed())
                    ), false);
                }
            }

            if (speedDownKey.wasPressed()) {
                float current = FlightMixin.getFlySpeed();
                FlightMixin.setFlySpeed(current - 0.05F);
                if (client.player != null) {
                    client.player.sendMessage(new LiteralText(
                        "Fly Speed: " + String.format("%.2f", FlightMixin.getFlySpeed())
                    ), false);
                }
            }
        });
    }
}