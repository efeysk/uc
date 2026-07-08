package com.example.flighthack;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class FlightMixin {
    private static boolean flightEnabled = false;
    private static boolean bypassActive = true;
    private static boolean groundSpoof = true;
    private static float flySpeed = 0.1F;
    private static double verticalSpeed = 0.5;
    private static double fallOffset = -0.0625;
    private static int spoofInterval = 2;
    private static int groundSpoofInterval = 3;
    private static int tickCounter = 0;
    private static double lastReportedY = 0.0;

    @Inject(method = "tick", at = @At("HEAD"))

    private void onTick(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
		
        if (player == null || !flightEnabled) return;
        tickCounter++;

        if (!player.getAbilities().flying) {
            player.getAbilities().flying = true;
            player.getAbilities().setFlySpeed(flySpeed);
        }

        Vec3d vel = player.getVelocity();
		
        if (mc.options.keyJump.isPressed()) {
            player.setVelocity(vel.x, verticalSpeed, vel.z);
        }

        if (mc.options.keySneak.isPressed()) {
            player.setVelocity(vel.x, -verticalSpeed, vel.z);
        }

        if (bypassActive) {
            if (tickCounter % spoofInterval == 0) {
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            }

            if (tickCounter % groundSpoofInterval == 0 && groundSpoof) {
                double currentY = player.getY();
                if (Math.abs(currentY - lastReportedY) > 0.1) {
                    player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        player.getX(), currentY - 0.001, player.getZ(), true
                    ));
                    lastReportedY = currentY;
                }
            }

            if (tickCounter % 5 == 0) {
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(
                    player, ClientCommandC2SPacket.Mode.START_SPRINTING
                ));
            }
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)

    private void onSendMovement(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || !flightEnabled || !bypassActive) return;
        double yOffset = fallOffset;
        double originalY = player.getY();
        double modifiedY = originalY + yOffset;

        player.setPosition(player.getX(), modifiedY, player.getZ());

        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
            player.getX(), modifiedY, player.getZ(), true
        ));

        player.setPosition(player.getX(), originalY, player.getZ());

        if (player.age % 2 == 0) {
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        }

        ci.cancel();
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || !flightEnabled) return;
        if (bypassActive && player.isOnGround()) {
            player.setVelocity(player.getVelocity().x, 0.0, player.getVelocity().z);
        }
    }

    public static void toggleFlight() {
        flightEnabled = !flightEnabled;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            if (!flightEnabled) {
                mc.player.getAbilities().flying = false;
                mc.player.getAbilities().setFlySpeed(0.05F);
                mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);
            } else {
                mc.player.getAbilities().flying = true;
                mc.player.getAbilities().setFlySpeed(flySpeed);
                lastReportedY = mc.player.getY();
            }
        }
    }


    public static void setBypass(boolean state) {
        bypassActive = state;
    }

    public static void setGroundSpoof(boolean state) {
        groundSpoof = state;
    }

    public static void setFlySpeed(float speed) {
        flySpeed = Math.min(Math.max(speed, 0.01F), 0.5F);
    }

    public static void setVerticalSpeed(double speed) {
        verticalSpeed = Math.min(Math.max(speed, 0.1), 1.0);
    }

    public static void setSpoofInterval(int ticks) {
        spoofInterval = Math.max(ticks, 1);
    }

    public static boolean isFlightEnabled() { return flightEnabled; }
    public static boolean isBypassActive() { return bypassActive; }
    public static float getFlySpeed() { return flySpeed; }
}