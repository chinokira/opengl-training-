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
