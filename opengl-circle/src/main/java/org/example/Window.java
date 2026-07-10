package org.example;

import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private long window;

    private final int width = 600;
    private final int height = 600;

    private final List<Sphere> spheres = new ArrayList<>();
    
    private ShaderProgram sphereShader;

    private Box box;

    private int frameCount = 0;
    private int currentFps = 0;
    private float fpsTimer = 0.0f;

    private float cameraYaw = -25.0f;
    private float cameraPitch = 20.0f;
    private float cameraDistance = 4.0f;

    private final float gravity = -9.81f;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Impossible d'initialiser GLFW");
        }

        window = glfwCreateWindow(width, height, "Simulation 3D LWJGL", NULL, NULL);

        if (window == NULL) {
            throw new RuntimeException("Impossible de créer la fenêtre");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        GL.createCapabilities();

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(window, true);
            }

            if (key == GLFW_KEY_R && action == GLFW_PRESS) {
                cameraYaw = -25.0f;
                cameraPitch = 20.0f;
                cameraDistance = 4.0f;
            }
        });

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        createShaders();

        box = new Box();

        glfwShowWindow(window);

        createSpheres();
    }


    private void createSpheres() {
        int count = 80;

        for (int i = 0; i < count; i++) {
            float radius = 0.08f;

            float x = -0.6f + (i % 5) * 0.25f;
            float y = 0.5f + ((i / 5) % 5) * 0.25f;
            float z = -0.5f + (i / 25) * 0.25f;

            x += ((float) Math.random() - 0.5f) * 0.05f;
            y += ((float) Math.random() - 0.5f) * 0.05f;
            z += ((float) Math.random() - 0.5f) * 0.05f;

            Sphere sphere = new Sphere(x, y, z, radius);

            sphere.setVelocity(
                    ((float) Math.random() - 0.5f) * 0.2f,
                    0.0f,
                    ((float) Math.random() - 0.5f) * 0.2f
            );

            spheres.add(sphere);
        }
    }

    private void loop() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        float lastTime = (float) glfwGetTime();

        while (!glfwWindowShouldClose(window)) {
            float currentTime = (float) glfwGetTime();
            float deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            if (deltaTime > 0.016f) {
                deltaTime = 0.016f;
            }

            updateWindowTitle(deltaTime);
            handleInput(deltaTime);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            setupCamera();

            for (Sphere sphere : spheres) {
                sphere.update(deltaTime, gravity);
                sphere.keepInsideBox(box);
            }

            for (int i = 0; i < 8; i++) {
                resolveSphereCollisions();

                for (Sphere sphere : spheres) {
                    sphere.keepInsideBox(box);
                }
            }

            for (Sphere sphere : spheres) {
                sphere.stopIfVerySlow();
                sphere.applyDamping();
            }

            box.drawBox3D();

            sphereShader.use();

            for (Sphere sphere : spheres) {
                sphere.draw();
            }

            sphereShader.stop();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void updateWindowTitle(float deltaTime) {
        frameCount++;
        fpsTimer += deltaTime;

        if (fpsTimer >= 1.0f) {
            currentFps = frameCount;
            frameCount = 0;
            fpsTimer = 0.0f;

            glfwSetWindowTitle(
                    window,
                    "Simulation 3D LWJGL | FPS: " + currentFps + " | Spheres: " + spheres.size()
            );
        }
    }

    private void handleInput(float deltaTime) {
        float rotationSpeed = 90.0f;
        float zoomSpeed = 2.0f;

        if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) {
            cameraYaw -= rotationSpeed * deltaTime;
        }

        if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) {
            cameraYaw += rotationSpeed * deltaTime;
        }

        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
            cameraPitch += rotationSpeed * deltaTime;
        }

        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
            cameraPitch -= rotationSpeed * deltaTime;
        }

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            cameraDistance -= zoomSpeed * deltaTime;
        }

        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            cameraDistance += zoomSpeed * deltaTime;
        }

        if (cameraPitch > 89.0f) {
            cameraPitch = 89.0f;
        }

        if (cameraPitch < -89.0f) {
            cameraPitch = -89.0f;
        }

        if (cameraDistance < 2.0f) {
            cameraDistance = 2.0f;
        }

        if (cameraDistance > 8.0f) {
            cameraDistance = 8.0f;
        }
    }

    private void resolveSphereCollisions() {
        for (int i = 0; i < spheres.size(); i++) {
            for (int j = i + 1; j < spheres.size(); j++) {
                spheres.get(i).resolveCollision(spheres.get(j));
            }
        }
    }

    private void setupCamera() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        float aspect = (float) width / height;
        float fov = 60.0f;
        float near = 0.1f;
        float far = 100.0f;

        float top = (float) Math.tan(Math.toRadians(fov) / 2.0f) * near;
        float bottom = -top;
        float right = top * aspect;
        float left = -right;

        glFrustum(left, right, bottom, top, near, far);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glTranslatef(0.0f, 0.0f, -cameraDistance);
        glRotatef(cameraPitch, 1.0f, 0.0f, 0.0f);
        glRotatef(cameraYaw, 0.0f, 1.0f, 0.0f);
    }

    private void createShaders() {
        String vertexShader = """
                #version 120

                varying vec3 vNormal;

                void main() {
                    vNormal = normalize(gl_NormalMatrix * gl_Normal);
                    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
                }
                """;

        String fragmentShader = """
                #version 120

                varying vec3 vNormal;

                void main() {
                    vec3 normal = normalize(vNormal);
                    vec3 lightDirection = normalize(vec3(0.4, 0.8, 0.6));

                    float diffuse = max(dot(normal, lightDirection), 0.0);

                    vec3 baseColor = vec3(0.0, 0.45, 1.0);
                    vec3 ambient = baseColor * 0.25;
                    vec3 finalColor = ambient + baseColor * diffuse * 0.85;

                    gl_FragColor = vec4(finalColor, 1.0);
                }
                """;

        sphereShader = new ShaderProgram(vertexShader, fragmentShader);
    }

    private void cleanup() {
        glfwDestroyWindow(window);
        glfwTerminate();
    }
}