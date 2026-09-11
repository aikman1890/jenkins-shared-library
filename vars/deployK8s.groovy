#!/usr/bin/env groovy
/**
 * deployK8s - deploy a Helm chart to Kubernetes, with a manual approval gate
 * for production.
 *
 * Usage:
 *   deployK8s(env: 'staging', release: 'myapp', chart: './helm/myapp',
 *            namespace: 'apps-staging', values: ['image.tag': "${env.BUILD_NUMBER}"])
 *
 * A kubeconfig file credential must be bound as 'kubeconfigFile'.
 * When env == 'prod', the pipeline pauses for manual approval (12h timeout)
 * before running helm upgrade.
 */
def call(Map config = [:]) {
    String envName   = config.env       ?: error('deployK8s: "env" is required')
    String release   = config.release   ?: error('deployK8s: "release" is required')
    String chart     = config.chart     ?: error('deployK8s: "chart" is required')
    String namespace = config.namespace ?: "${release}-${envName}"
    Map values       = config.values    ?: [:]

    stage("Deploy to ${envName}") {
        if (envName == 'prod') {
            timeout(time: 12, unit: 'HOURS') {
                input message: "Approve deployment of ${release} to PRODUCTION?",
                      submitter: 'release-managers',
                      parameters: [
                          string(name: 'CONFIRM', defaultValue: '',
                                 description: 'Type YES to confirm the production deploy')
                      ]
            }
        }

        withCredentials([file(credentialsId: 'kubeconfigFile', variable: 'KUBECONFIG')]) {
            String setArgs = values.collect { k, v -> "--set ${k}=${v}" }.join(' ')
            sh """
                helm upgrade --install ${release} ${chart} \
                    --namespace ${namespace} --create-namespace \
                    --wait --timeout 10m ${setArgs}
                kubectl rollout status deploy/${release} -n ${namespace} --timeout=5m
            """
        }
    }
}
