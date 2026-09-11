#!/usr/bin/env groovy
/**
 * notifySlack - post the build status to a Slack channel.
 *
 * Usage:
 *   notifySlack('#deploys')
 *   notifySlack('#deploys', "Build #${env.BUILD_NUMBER} ${currentBuild.currentResult}")
 *
 * Uses the Jenkins Slack plugin with the bot token from the global config.
 * Always call this in a post block so it fires on success and failure alike.
 */
def call(String channel, String message = null) {
    String result  = currentBuild.currentResult ?: 'UNKNOWN'
    String color   = [SUCCESS: 'good', FAILURE: 'danger', UNSTABLE: 'warning'].get(result, '#808080')
    String text    = message ?: "Build #${env.BUILD_NUMBER} (${env.JOB_NAME}) finished: ${result}"

    slackSend(
        channel: channel,
        color: color,
        message: "${text}\n${env.BUILD_URL}"
    )
}
