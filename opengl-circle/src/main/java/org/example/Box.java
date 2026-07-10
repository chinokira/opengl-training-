package org.example;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glVertex3f;

public class Box {

    private final float boxLeft = -1.0f;
    private final float boxRight = 1.0f;
    private final float boxBottom = -1.0f;
    private final float boxTop = 1.0f;
    private final float boxBack = -1.0f;
    private final float boxFront = 1.0f;


    public void drawBox3D() {
        glColor3f(1.0f, 1.0f, 1.0f);
        glLineWidth(2.0f);

        glBegin(GL_LINES);

        glVertex3f(boxLeft, boxBottom, boxBack);
        glVertex3f(boxRight, boxBottom, boxBack);

        glVertex3f(boxRight, boxBottom, boxBack);
        glVertex3f(boxRight, boxTop, boxBack);

        glVertex3f(boxRight, boxTop, boxBack);
        glVertex3f(boxLeft, boxTop, boxBack);

        glVertex3f(boxLeft, boxTop, boxBack);
        glVertex3f(boxLeft, boxBottom, boxBack);

        glVertex3f(boxLeft, boxBottom, boxFront);
        glVertex3f(boxRight, boxBottom, boxFront);

        glVertex3f(boxRight, boxBottom, boxFront);
        glVertex3f(boxRight, boxTop, boxFront);

        glVertex3f(boxRight, boxTop, boxFront);
        glVertex3f(boxLeft, boxTop, boxFront);

        glVertex3f(boxLeft, boxTop, boxFront);
        glVertex3f(boxLeft, boxBottom, boxFront);

        glVertex3f(boxLeft, boxBottom, boxBack);
        glVertex3f(boxLeft, boxBottom, boxFront);

        glVertex3f(boxRight, boxBottom, boxBack);
        glVertex3f(boxRight, boxBottom, boxFront);

        glVertex3f(boxRight, boxTop, boxBack);
        glVertex3f(boxRight, boxTop, boxFront);

        glVertex3f(boxLeft, boxTop, boxBack);
        glVertex3f(boxLeft, boxTop, boxFront);

        glEnd();

        glLineWidth(1.0f);
    }

    public float getLeft() {
        return boxLeft;
    }

    public float getRight() {
        return boxRight;
    }

    public float getBottom() {
        return boxBottom;
    }

    public float getTop() {
        return boxTop;
    }

    public float getBack() {
        return boxBack;
    }

    public float getFront() {
        return boxFront;
    }
}
