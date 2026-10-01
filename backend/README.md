# SkillCert API

Spring Boot 3 / Java 21 API for SkillCert. It runs Flyway migrations automatically and exposes the first assessment flow.

## Local run

1. Install a Java 21 JDK and Maven.
2. Create a Supabase PostgreSQL project, then copy `.env.example` values into your shell environment.
3. Run `mvn spring-boot:run` inside this directory.

## Razorpay (local test mode)

The credential costs **₹199**. Set `RAZORPAY_KEY_ID` and
`RAZORPAY_KEY_SECRET` to Razorpay **Test Mode** credentials before starting
the API. The browser receives only the key ID; the secret stays on the server.

After a user completes Checkout, the API verifies the Razorpay signature and
checks that the payment is captured before it issues a certificate. Configure a
`payment.captured` webhook with the public production endpoint
`POST /api/v1/payments/razorpay/webhook`, then set `RAZORPAY_WEBHOOK_SECRET`.
For local development, Checkout verification works without a webhook; use a
public tunnel only when testing webhook delivery.

Endpoints:

- `GET /api/v1/certifications`
- `GET /api/v1/certifications/cloud-foundations/assessment`
- `POST /api/v1/certifications/cloud-foundations/attempts`
- `POST /api/v1/payments/razorpay/orders`
- `POST /api/v1/payments/razorpay/verify`
- `GET /actuator/health`

The attempt API is server-scored. Correct option flags are never sent to the browser.
