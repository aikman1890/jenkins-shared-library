#!/usr/bin/env groovy
/**
 * securityScan - scan a container image with Trivy and run SonarQube analysis.
 *
 * Usage:
 *   securityScan(image: 'myregistry.example.com/team/app:42', failOn: 'HIGH,CRITICAL')
 *
 * Expects `trivy` on the agent PATH and a SonarQube server configured in
 * Jenkins under the name 'sonarqube'.
 */
def call(Map config = [:]) {
    String image  = config.image  ?: error('securityScan: "image" is required')
    String failOn = config.failOn ?: 'HIGH,CRITICAL'

    stage('Security Scan') {
        parallel 'Trivy Image Scan': {
            sh """
                trivy image --severity ${failOn} --exit-code 1 --no-progress ${image}
            """
        }, 'SonarQube Analysis': {
            withSonarQubeEnv('sonarqube') {
                sh 'sonar-scanner'
            }
            // Enforce the quality gate before the pipeline continues.
            timeout(time: 10, unit: 'MINUTES') {
                waitForQualityGate(abortPipeline: true)
            }
        }
    }
}
