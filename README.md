
# direct-debit-update-email-frontend

### Test-Only Start Page
A test-only start page is available via the URL: 
```
/direct-debit-verify-email-address/test-only/start
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
  -d '{ "token": "12345678", "principal": "direct-debit-update-email-frontend", "permissions": [ { "resourceType": "direct-debit-update-email-backend", "resourceLocation": "direct-debit-update-email/bta/start", "actions": ["WRITE"]  }, { "resourceType": "direct-debit-update-email-backend", "resourceLocation": "direct-debit-update-email/epaye/start", "actions": ["WRITE"]  } ] }' \
  http://localhost:8470/test-only/token
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").