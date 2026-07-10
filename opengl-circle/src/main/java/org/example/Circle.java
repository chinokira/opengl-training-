package org.example;

import static org.lwjgl.opengl.GL11.*;

public class Circle {

    private float x;
    private float y;
    private float radius;
    private float velocityX;
    private float velocityY;
    private float bounce = 0.9f;

    /**
     * Constructor of the Circle class
     * @param x
     * @param y
     * @param radius
     */
    public Circle(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    /**
     * Draw the circle
     */
    public void draw() {
        glBegin(GL_TRIANGLE_FAN);
        glColor3f(0.0f, 0.5f, 1.0f);

        glVertex2f(x, y);

        for (int i = 0; i <= 360; i++) {
            double angle = Math.toRadians(i);

            float pointX = x + (float) Math.cos(angle) * radius;
            float pointY = y + (float) Math.sin(angle) * radius;

            glVertex2f(pointX, pointY);
        }

        glEnd();
    }

    public void move(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setVelocity(float x, float y) {
        this.velocityX = x;
        this.velocityY = y;
    }

    public void update(float deltaTime, float gravity) {
        velocityY += gravity * deltaTime;

        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
    }
    public void keepInsideBox(float left, float right, float bottom, float top) {
        if (x - radius < left) {
            x = left + radius;
            velocityX = -velocityX * bounce;
        }

        if (x + radius > right) {
            x = right - radius;
            velocityX = -velocityX * bounce;
        }

        if (y - radius < bottom) {
            y = bottom + radius;

            if (Math.abs(velocityY) < 0.5f) {
                velocityY = 0.0f;
            } else {
                velocityY = -velocityY * bounce;
            }

            velocityX *= 0.95f;
        }

        if (y + radius > top) {
            y = top - radius;
            velocityY = -velocityY * bounce;
        }
    }

    public void resolveCollision(Circle other) {
        float dx = other.x - this.x;
        float dy = other.y - this.y;

        float distanceSquared = dx * dx + dy * dy;
        float minDistance = this.radius + other.radius;

        if (distanceSquared >= minDistance * minDistance || distanceSquared == 0.0f) {
            return;
        }

        float distance = (float) Math.sqrt(distanceSquared);

        float normalX = dx / distance;
        float normalY = dy / distance;

        float overlap = minDistance - distance;

        this.x -= normalX * overlap / 2.0f;
        this.y -= normalY * overlap / 2.0f;

        other.x += normalX * overlap / 2.0f;
        other.y += normalY * overlap / 2.0f;

        float relativeVelocityX = other.velocityX - this.velocityX;
        float relativeVelocityY = other.velocityY - this.velocityY;

        float velocityAlongNormal = relativeVelocityX * normalX + relativeVelocityY * normalY;

        if (velocityAlongNormal > 0) {
            return;
        }

        float restitution = 0.8f;
        float impulse = -(1.0f + restitution) * velocityAlongNormal;
        impulse /= 2.0f;

        float impulseX = impulse * normalX;
        float impulseY = impulse * normalY;

        this.velocityX -= impulseX;
        this.velocityY -= impulseY;

        other.velocityX += impulseX;
        other.velocityY += impulseY;

        float friction = 0.98f;

        this.velocityX *= friction;
        other.velocityX *= friction;
    }

    public void stopIfVerySlow() {
        float threshold = 0.02f;

        if (Math.abs(velocityX) < threshold) {
            velocityX = 0.0f;
        }

        if (Math.abs(velocityY) < threshold) {
            velocityY = 0.0f;
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRadius() {
        return radius;
    }
}