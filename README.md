
# direct-debit-update-email-frontend

### Test-Only Start Page
A test-only start page is available via the URL: 
```
/direct-debit-verify-email/test-only/start
```
In order for this page to work the internal-auth tokens in config (default=`1234567`) must have the correct permissions set
to be able to call the start endpoints in direct-debit-update-email-backend. Locally this can be achieved by ensuring 
the internal-auth service is running via:
```bash
sm --start INTERNAL_AUTH -r
```
and then running the following curl command:
```bash
curl -v -X POST  \
  -H "Content-Type: application/json" \
  -d '{ "token": "1234567", "principal": "direct-debit-update-email-frontend", "permissions": [ { "resourceType": "direct-debit-update-email-backend", "resourceLocation": "direct-debit-update-email/bta/start", "actions": ["WRITE"]  }, { "resourceType": "direct-debit-update-email-backend", "resourceLocation": "direct-debit-update-email/epaye/start", "actions": ["WRITE"]  } ] }' \
  http://localhost:8470/test-only/token
```

### Test-Only Get Passcodes Endpoint
When an email verification journey is started, the correct passcode to enter is generated in an external service. In order
to facilitate testing, a test-only endpoint has been created in this service to return the list of email addresses and passcodes
generated for them by the external service. This will allow the correct passcode to be entered when prompted and thus allow for the 
happy-path journey. The endpoint can be reached via the url:
```
/direct-debit-verify-email/test-only/email-verification-passcodes
```
The endpoint requires a valid GG session. If you are navigating through the journey on a browser, you will be able to
hit this endpoint in a new tab and be able to see the passcodes for the current session.

The endpoint will return JSON content  - an example output from the page is:
```
{
  "passcodes" : [ {
    "email" : "bounced@email.com",
    "passcode" : "JDCRQQ"
  }, {
    "email" : "a@b.com",
    "passcode" : "BZHRTD"
  }, {
    "email" : "a@b.com",
    "passcode" : "KBKNCN"
  } ]
}
```
The above output means that one passcode has been generated for `bounced@email.com` and two passcodes have been generated 
for `a@b.com` in the same session. Only the latest generated code will be valid - there is however no guarantee of order 
in the returned list above. 

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").