package com.dev.leavesHack.events;

import meteordevelopment.meteorclient.events.Cancellable;

public class RotateEvent extends Cancellable {
    private float yaw;
    private float pitch;
    private boolean modified;
    public RotateEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYRot() {
        return this.yaw;
    }

    public void setYRot(float yaw) {
        modified = true;
        setYawNoModify(yaw);
    }

    public float getXRot() {
        return this.pitch;
    }

    public void setXRot(float pitch) {
        modified = true;
        setPitchNoModify(pitch);
    }

    public boolean isModified() {
        return modified;
    }
    public void setRotation(final float yaw, final float pitch) {
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    public void setYawNoModify(float yaw) {
        this.yaw = yaw;
    }

    public void setPitchNoModify(float pitch) {
        this.pitch = pitch;
    }
}
