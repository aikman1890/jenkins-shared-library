#!/usr/bin/env groovy
/**
 * buildApp - build a Docker image and push it to a registry.
 *
 * Usage:
 *   buildApp(registry: 'myregistry.example.com', image: 'team/app', tag: "${env.BUILD_NUMBER}")
 *
 * Requires a Jenkins username/password credential bound as
 * 'dockerRegistryCreds' with registry login rights.
 */
def call(Map config = [:]) {
    String registry = config.registry ?: 'myregistry.example.com'
    String image    = config.image    ?: error('buildApp: "image" is required')
    String tag      = config.tag      ?: "${env.BUILD_NUMBER}"
    String context  = config.context  ?: '.'
    String dockerfile = config.dockerfile ?: 'Dockerfile'

    String fullTag = "${registry}/${image}:${tag}"

    stage('Build & Push') {
        docker.withRegistry("https://${registry}", 'dockerRegistryCreds') {
            def app = docker.build(fullTag, "--file ${dockerfile} ${context}")
            app.push()
            app.push('latest')
        }
        echo "Pushed ${fullTag}"
    }
}
