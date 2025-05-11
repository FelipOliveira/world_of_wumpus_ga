package com.foliveira.utils.debug;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Logger;

public class DebugCameraController {
    private static final Logger log = new Logger(DebugCameraController.class.getName(), Logger.DEBUG);

    private Vector2 position = new Vector2();
    private Vector2 startPosition = new Vector2();
    private float zoom = 1.0f;
    private DebugCameraConfig config;

    public DebugCameraController() {
        config = new DebugCameraConfig();
        log.info("cameraConfig= " + config);
    }

    public void setStartPosition(float x, float y) {
        startPosition.set(x, y);
        position.set(x, y);
    }

    public void applyToCamera(OrthographicCamera camera) {
        camera.position.set(position, 0);
        camera.zoom = zoom;
        camera.update();
    }

    public void inputDebugHandle(float delta) {
        if (Gdx.app.getType() != Application.ApplicationType.Desktop) {
            return;
        }
        float moveSpeed = config.getMoveSpeed() * delta;
        float zoomSpeed = config.getZoomSpeed() * delta;

        //move controls
        if (config.isLeftPressed()) moveLeft(moveSpeed);
        else if (config.isRightPressed()) moveRight(moveSpeed);
        else if (config.isUpPressed()) moveUp(moveSpeed);
        else if (config.isDownPressed()) moveDown(moveSpeed);

        //zoom controls
        if (config.isZoomInPressed()) zoomIn(zoomSpeed);
        else if (config.isZoomOutPressed()) zoomOut(zoomSpeed);

        //reset controls
        if (config.isResetPressed()) resetControl();

        //log controls
        if (config.isLogPressed()) LogDebug();
    }

    private void LogDebug() {
        log.debug("camera zoom:" + zoom);
    }

    private void resetControl() {
        position.set(startPosition);
        setZoom(1.0f);
    }

    private void zoomOut(float zoomSpeed) {
        setZoom(zoom + zoomSpeed);
    }

    private void zoomIn(float zoomSpeed) {
        setZoom(zoom - zoomSpeed);
    }

    private void setZoom(float value) {
        zoom = MathUtils.clamp(value, config.getMaxZoomIn(), config.getMaxZoomOut());
    }

    private void setPosition(float x, float y) {
        position.set(x, y);
    }

    private void moveCamera(float xSpeed, float ySpeed) {
        setPosition(position.x + xSpeed, position.y + ySpeed);
    }

    private void moveLeft(float speed) {
        moveCamera(-speed, 0);
    }

    private void moveRight(float speed) {
        moveCamera(speed, 0);
    }

    private void moveUp(float speed) {
        moveCamera(0, speed);
    }

    private void moveDown(float speed) {
        moveCamera(0, -speed);
    }

}
