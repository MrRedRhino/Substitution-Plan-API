#### Substitution Plan API

---

### API Specification

`GET` **/api/plans/today?format={format}** \
Returns today's substitution plan. \
`{format}` must be one of the following values:
- json
- html
- pdf
- png

`GET` **/api/plans/tomorrow?format={format}:** \
Returns tomorrow's substitution plan. \
`{format}` must be one of the following values:
- json
- html
- pdf
- png

`PUT` **/api/subscriptions** \
Creates a web-push notification subscription.
Required body format:
```json
{
  "endpoint": "URL to send the notifications to",
  "key": "p256dh key to encrypt notifications",
  "auth": "Auth key to encrypt notifications"
}
```

`GET` **/api/subscriptions/{endpoint}** \
Returns status 400 when no subscription with the given `endpoint` exists
or the configured filter.

`PATCH` **/api/subscriptions/{endpoint}** \
Updates the configured filter for the subscription with the given `endpoint`.
Returns status 400 when no subscription with the given `endpoint` exists.

`DELETE` **/api/subscriptions/{endpoint}** \
Removes the subscription with the given `endpoint`. 
Returns status 400 when no such subscription exists. 
