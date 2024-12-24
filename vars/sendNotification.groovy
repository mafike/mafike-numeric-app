def call(String buildStatus = 'STARTED') {
 buildStatus = buildStatus ?: 'SUCCESS'
    // Set color and emoji based on build status
    def color
    def emoji
    if (buildStatus == 'SUCCESS') {
        color = '#47ec05'
        emoji = ':ww:'
    } else if (buildStatus == 'UNSTABLE') {
        color = '#d5ee0d'
        emoji = ':deadpool:'
    } else {
        color = '#ec2805'
        emoji = ':hulk:'
    }

    // Validate and set fallback defaults for environment variables
    def deploymentName = env.deploymentName ?: "Unknown Deployment"
    def applicationURL = env.applicationURL ?: "http://default-application-url.com"
    def gitURL = env.GIT_URL ?: "http://default-git-url.com"
    def failedStage = env.failedStage ?: "No Failed Stage"
    def branchName = env.BRANCH_NAME ?: "Unknown Branch"
    def buildURL = env.BUILD_URL ?: "http://default-build-url.com"
>>>>>>> develop

    // Slack attachments
    def attachments = [
        [
            "color": color,
            "blocks": [
                [
                    "type": "header",
                    "text": [
                        "type": "plain_text",
                        "text": "K8S Deployment - ${deploymentName} Pipeline ${emoji}",
                        "emoji": true
                    ]
                ],
                [
                    "type": "section",
                    "fields": [
                        [
                            "type": "mrkdwn",
                            "text": "*Job Name:*\n${env.JOB_NAME}"
                        ],
                        [
                            "type": "mrkdwn",
                            "text": "*Build Number:*\n${env.BUILD_NUMBER}"
                        ],
                        [
                            "type": "mrkdwn",
                            "text": "*Branch:*\n${branchName}"
                        ]
                    ],
                    "accessory": [
                        "type": "image",
                        "image_url": "https://raw.githubusercontent.com/mafike/dev-sec-proj/refs/heads/master/slack-emojis/jenkins.png",
                        "alt_text": "Slack Icon"
                    ]
                ],
                [
                    "type": "section",
                    "text": [
                        "type": "mrkdwn",
                        "text": "*Failed Stage Name:* `${failedStage}`"
                    ],
                    "accessory": [
                        "type": "button",
                        "text": [
                            "type": "plain_text",
                            "text": "Jenkins Build URL",
                            "emoji": true
                        ],
                        "value": "click_me_123",
                        "url": "${buildURL}",
                        "action_id": "button-action"
                    ]
                ],
                [
                    "type": "divider"
                ],
                [
                    "type": "section",
                    "fields": [
                        [
                            "type": "mrkdwn",
                            "text": "*Kubernetes Deployment Name:*\n${deploymentName}"
                        ],
                        [
                            "type": "mrkdwn",
                            "text": "*Application URL:* <${applicationURL}:32564|Access App>"
                        ]
                    ]
                ],
                [
                    "type": "section",
                    "fields": [
                        [
                            "type": "mrkdwn",
                            "text": "*Git Commit:*\n${env.GIT_COMMIT ?: 'Unknown Commit'}"
                        ],
                        [
                            "type": "mrkdwn",
                            "text": "*Previous Commit:*\n${env.GIT_PREVIOUS_COMMIT ?: 'None'}"
                        ]
                    ],
                    "accessory": [
                        "type": "image",
                        "image_url": "https://raw.githubusercontent.com/mafike/dev-sec-proj/refs/heads/master/slack-emojis/github.png",
                        "alt_text": "Github Icon"
                    ]
                ]
            ]
        ]
    ]

    // Send Slack message
    slackSend(
        channel: env.SLACK_CHANNEL ?: 'default-channel',
        iconEmoji: emoji,
        attachments: attachments
    )
}
