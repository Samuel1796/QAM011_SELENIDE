#!/usr/bin/env bash
# Builds the Slack Block Kit JSON payload and writes it to /tmp/slack_payload.json.
# All dynamic values are passed as environment variables to avoid shell quoting issues.
set -euo pipefail

jq -n \
  --arg repo       "${REPO}" \
  --arg repo_url   "${REPO_URL}" \
  --arg branch     "${BRANCH}" \
  --arg actor      "${ACTOR}" \
  --arg sha        "${SHA}" \
  --arg sha_url    "${SHA_URL}" \
  --arg run_url    "${RUN_URL}" \
  --arg report_url "${REPORT_URL}" \
  --arg summary    "${SUMMARY}" \
  --arg failures   "${FAILURES}" \
  '{
    blocks: [
      {
        type: "header",
        text: { type: "plain_text", text: "❌ Test Suite Failed", emoji: true }
      },
      {
        type: "section",
        fields: [
          { type: "mrkdwn", text: ("*Repository:*\n<" + $repo_url + "|" + $repo + ">") },
          { type: "mrkdwn", text: ("*Branch:*\n`" + $branch + "`") },
          { type: "mrkdwn", text: ("*Triggered by:*\n" + $actor) },
          { type: "mrkdwn", text: ("*Commit:*\n<" + $sha_url + "|`" + ($sha | .[0:7]) + "`>") }
        ]
      },
      {
        type: "section",
        text: { type: "mrkdwn", text: ("*Test summary*\n" + $summary) }
      },
      {
        type: "section",
        text: { type: "mrkdwn", text: ("*Failed tests*\n" + $failures) }
      },
      {
        type: "actions",
        elements: [
          {
            type: "button",
            text: { type: "plain_text", text: "🔍 View Run Logs", emoji: true },
            url: $run_url
          },
          {
            type: "button",
            text: { type: "plain_text", text: "📊 Allure Report", emoji: true },
            url: $report_url
          }
        ]
      },
      {
        type: "context",
        elements: [
          { type: "mrkdwn", text: "Screenshots and full Allure results are attached as build artifacts in the run above." }
        ]
      }
    ]
  }' > /tmp/slack_payload.json
