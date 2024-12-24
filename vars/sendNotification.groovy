def call(String buildStatus = 'STARTED') {
 buildStatus = buildStatus ?: 'SUCCESS'

 def color

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

// def msg = "${buildStatus}: `${env.JOB_NAME}` #${env.BUILD_NUMBER}:\n${env.BUILD_URL}"

// slackSend(color: color, message: msg)

attachments = [
    [
        "color": color,
        "blocks": [
            [
                "type": "header",
                "text": [
                    "type": "plain_text",
                    "text": "K8S Deployment - ${deploymentName} Pipeline  ${env.emoji}",
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
                    ]
                ],
                "accessory": [
                    "type": "image",
                    "image_url": "https://raw.githubusercontent.com/mafike/mafike-numeric-app/main/slack-emojis/jenkins.png",
                    "alt_text": "Slack Icon"
                ]
            ],
            [
                "type": "section",
                "text": [
                    "type": "mrkdwn",
                    "text": "*Failed Stage Name:* `${env.failedStage ?: 'No Failed Stage'}`"
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
                    "image_url": "https://raw.githubusercontent.com/mafike/mafike-numeric-app/main/slack-emojis/github.png",
                    "alt_text": "Github Icon"
                ]
            ],
            [
                "type": "section",
                "text": [
                    "type": "mrkdwn",
                    "text": "*Git Branch:* `${env.BRANCH_NAME ?: 'Unknown Branch'}`"
                ],
                "accessory": [
                    "type": "button",
                    "text": [
                        "type": "plain_text",
                        "text": "Github Repo URL",
                        "emoji": true
                    ],
                    "value": "click_me_123",
                    "url": "${gitURL}",
                    "action_id": "button-action"
                ]
            ]
        ]
    ]
]

 slackSend(iconEmoji: emoji, attachments: attachments)

}
