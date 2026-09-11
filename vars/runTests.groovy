#!/usr/bin/env groovy
/**
 * runTests - run the test suite, publish JUnit results, fail on test failure.
 *
 * Usage:
 *   runTests(testCommand: 'pytest -q --junitxml=junit.xml', junitPattern: 'junit.xml')
 */
def call(Map config = [:]) {
    String testCommand  = config.testCommand  ?: 'pytest -q --junitxml=junit.xml'
    String junitPattern = config.junitPattern ?: 'junit.xml'

    stage('Test') {
        // Run the suite but let JUnit publishing decide the build result,
        // so a red suite always shows red tests in the UI.
        def exitCode = sh(script: testCommand, returnStatus: true)
        junit testResults: junitPattern, allowEmptyResults: false

        if (exitCode != 0) {
            error("runTests: test command failed with exit code ${exitCode}")
        }
    }
}
