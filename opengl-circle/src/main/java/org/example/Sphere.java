package org.example;

import static org.lwjgl.opengl.GL11.*;

public class Sphere {

    private float x;
    private float y;
    private float z;
    private float radius;

    private float velocityX;
    private float velocityY;
    private float velocityZ;

    private float bounce = 0.4f;

    /**
     * Constructor of the Sphere class
     * @param x
     * @param y
     * @param z
     * @param radius
     */
    public Sphere(float x, float y, float z, float radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
    }

    /**
     * Draw the sphere
     */
    public void draw() {
        int stacks = 12;
        int slices = 24;

        for (int i = 0; i < stacks; i++) {
            double lat0 = Math.PI * (-0.5 + (double) i / stacks);
            double z0 = Math.sin(lat0);
            double zr0 = Math.cos(lat0);

            double lat1 = Math.PI * (-0.5 + (double) (i + 1) / stacks);
            double z1 = Math.sin(lat1);
            double zr1 = Math.cos(lat1);

            glBegin(GL_QUAD_STRIP);

            for (int j = 0; j <= slices; j++) {
                double lng = 2 * Math.PI * (double) j / slices;
                double cos = Math.cos(lng);
                double sin = Math.sin(lng);

                float normalX0 = (float) (cos * zr0);
                float normalY0 = (float) (sin * zr0);
                float normalZ0 = (float) z0;

                float normalX1 = (float) (cos * zr1);
                float normalY1 = (float) (sin * zr1);
                float normalZ1 = (float) z1;

                glNormal3f(normalX0, normalY0, normalZ0);
                glVertex3f(
                        x + normalX0 * radius,
                        y + normalY0 * radius,
                        z + normalZ0 * radius
                );

                glNormal3f(normalX1, normalY1, normalZ1);
                glVertex3f(
                        x + normalX1 * radius,
                        y + normalY1 * radius,
                        z + normalZ1 * radius
                );
            }

            glEnd();
        }
    }

    /**
     * Update the sphere position
     * @param deltaTime
     * @param gravity
     */
    public void update(float deltaTime, float gravity) {
        velocityY += gravity * deltaTime;

        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        z += velocityZ * deltaTime;
    }

    /**
     * Keep the sphere inside the box
     * @param box
     */
    public void keepInsideBox(Box box) {
        float left = box.getLeft();
        float right = box.getRight();
        float bottom = box.getBottom();
        float top = box.getTop();
        float back = box.getBack();
        float front = box.getFront();

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
            velocityY = -velocityY * bounce;
            velocityX *= 0.95f;
            velocityZ *= 0.95f;
        }

        if (y + radius > top) {
            y = top - radius;
            velocityY = -velocityY * bounce;
        }

        if (z - radius < back) {
            z = back + radius;
            velocityZ = -velocityZ * bounce;
        }

        if (z + radius > front) {
            z = front - radius;
            velocityZ = -velocityZ * bounce;
        }
    }

    /**
     * Resolve the collision between the sphere and another sphere
     * @param other
     */
    public void resolveCollision(Sphere other) {
        float dx = other.x - this.x;
        float dy = other.y - this.y;
        float dz = other.z - this.z;

        float distanceSquared = dx * dx + dy * dy + dz * dz;
        float minDistance = this.radius + other.radius;

        if (distanceSquared >= minDistance * minDistance) {
            return;
        }

        if (distanceSquared == 0.0f) {
            dx = 0.001f;
            dy = 0.0f;
            dz = 0.0f;
            distanceSquared = dx * dx + dy * dy + dz * dz;
        }

        float distance = (float) Math.sqrt(distanceSquared);

        float normalX = dx / distance;
        float normalY = dy / distance;
        float normalZ = dz / distance;

        float overlap = minDistance - distance;

        this.x -= normalX * overlap / 2.0f;
        this.y -= normalY * overlap / 2.0f;
        this.z -= normalZ * overlap / 2.0f;

        other.x += normalX * overlap / 2.0f;
        other.y += normalY * overlap / 2.0f;
        other.z += normalZ * overlap / 2.0f;

        float relativeVelocityX = other.velocityX - this.velocityX;
        float relativeVelocityY = other.velocityY - this.velocityY;
        float relativeVelocityZ = other.velocityZ - this.velocityZ;

        float velocityAlongNormal =
                relativeVelocityX * normalX +
                        relativeVelocityY * normalY +
                        relativeVelocityZ * normalZ;

        if (velocityAlongNormal > 0) {
            return;
        }

        float restitution = 0.05f;
        float impulse = -(1.0f + restitution) * velocityAlongNormal;
        impulse /= 2.0f;

        float impulseX = impulse * normalX;
        float impulseY = impulse * normalY;
        float impulseZ = impulse * normalZ;

        this.velocityX -= impulseX;
        this.velocityY -= impulseY;
        this.velocityZ -= impulseZ;

        other.velocityX += impulseX;
        other.velocityY += impulseY;
        other.velocityZ += impulseZ;
    }

    /**
     * Set the sphere velocity
     * @param x
     * @param y
     * @param z
     */
    public void setVelocity(float x, float y, float z) {
        this.velocityX = x;
        this.velocityY = y;
        this.velocityZ = z;
    }

    /**
     * Accelerate the sphere towards a point.
     */
    public void accelerateTowards(float targetX, float targetY, float targetZ,
                                  float strength, float deltaTime) {
        float dx = targetX - x;
        float dy = targetY - y;
        float dz = targetZ - z;
        float distanceSquared = dx * dx + dy * dy + dz * dz;

        // Avoid an unstable direction when the sphere is exactly at the target.
        if (distanceSquared < 0.000001f) {
            return;
        }

        float distance = (float) Math.sqrt(distanceSquared);
        float acceleration = strength * deltaTime;

        velocityX += dx / distance * acceleration;
        velocityY += dy / distance * acceleration;
        velocityZ += dz / distance * acceleration;
    }

    /**
     * Apply damping to the sphere velocity
     */
    public void applyDamping() {
        velocityX *= 0.995f;
        velocityY *= 0.995f;
        velocityZ *= 0.995f;
    }

    /**
     * Stop the sphere if it is very slow
     */
    public void stopIfVerySlow() {
        float threshold = 0.02f;

        if (Math.abs(velocityX) < threshold) {
            velocityX = 0.0f;
        }

        if (Math.abs(velocityY) < threshold) {
            velocityY = 0.0f;
        }

        if (Math.abs(velocityZ) < threshold) {
            velocityZ = 0.0f;
        }
    }
}
