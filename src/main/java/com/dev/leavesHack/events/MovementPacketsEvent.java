package com.dev.leavesHack.events;

import meteordevelopment.meteorclient.events.Cancellable;

public class MovementPacketsEvent extends Cancellable {
    private double x, y, z;
    private float yaw, pitch;
    private boolean ground;
    public MovementPacketsEvent(double x, double y, double z, float yaw, float pitch, boolean ground) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.ground = ground;
    }

    public float getYRot() {
        return this.yaw;
    }

    public void setYRot(float yaw) {
        this.yaw = yaw;
    }

    public float getXRot() {
        return this.pitch;
    }

    public void setXRot(float pitch) {
        this.pitch = pitch;
    }

    public void setRotation(final float yaw, final float pitch) {
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    public boolean isGround() {
        return ground;
    }

    public void setGround(boolean ground) {
        this.ground = ground;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }
}
