# Bulk Email Sender --- Setup Guide

This guide explains how to configure **Google Sign-In** and **Gmail
SMTP** for the Bulk Email Sender application.

> **Important:** Google Sign-In and Gmail SMTP are separate features.
> Signing in with Google does not automatically grant permission to send
> mail through that user's Gmail account. With an SMTP-based setup, mail
> is sent using the SMTP credentials configured by the application or
> user.

## 1. Requirements

-   Java version compatible with the project's `pom.xml`
-   Maven
-   Node.js and npm for the React frontend
-   A Google Cloud project
-   A Gmail account for SMTP testing

## 2. Configure Google Authentication

Google OAuth lets users sign in to the application.

### 2.1 Create a Google Cloud project

1.  Open the [Google Cloud Console](https://console.cloud.google.com/).
2.  Create a project or select an existing project.
3.  Open **Google Auth Platform** (or **APIs & Services → OAuth consent
    screen**, depending on the console UI).
4.  Configure the app name, support email, and contact email.
5.  If the app is in **Testing** mode, add the Google accounts allowed
    to sign in as test users.

### 2.2 Create OAuth client credentials

1.  Open **Google Auth Platform → Clients** or **APIs & Services →
    Credentials**.

2.  Create an **OAuth client ID** and choose **Web application**.

3.  Add this authorized redirect URI for local development:

    ``` text
    http://localhost:8080/login/oauth2/code/google
    ```

4.  If needed for your OAuth setup, add this frontend origin under
    **Authorized JavaScript origins**:

    ``` text
    http://localhost:5173
    ```

5.  Copy the generated **Client ID** and **Client Secret**. Keep the
    Client Secret private.

For production, add the backend's real HTTPS callback URL:

``` text
https://YOUR-BACKEND-DOMAIN/login/oauth2/code/google
```

Replace `YOUR-BACKEND-DOMAIN` with the deployed backend hostname. The
redirect URI must match the URI configured in Spring Security exactly.

### 2.3 Configure Spring Boot

In `src/main/resources/application.properties`, use environment-variable
placeholders instead of hardcoding credentials:

``` properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
```

Set these environment variables before starting the backend.

**Windows PowerShell (current terminal only):**

``` powershell
$env:GOOGLE_CLIENT_ID="YOUR_GOOGLE_CLIENT_ID"
$env:GOOGLE_CLIENT_SECRET="YOUR_GOOGLE_CLIENT_SECRET"
```

Then start the backend using your project's normal Maven or IDE run
configuration.

For a deployed service, add `GOOGLE_CLIENT_ID` and
`GOOGLE_CLIENT_SECRET` in the hosting provider's **Environment Variables
/ Secrets** settings. Never put the secret in React/Vite variables.

### 2.4 Google login URL

The frontend's **Continue with Google** button should navigate to the
backend OAuth endpoint:

``` text
http://localhost:8080/oauth2/authorization/google
```

In production, replace `http://localhost:8080` with the deployed backend
base URL.

After successful login, configure the backend's success handler to
redirect to the correct frontend URL for the environment.

## 3. Configure Gmail SMTP

Gmail SMTP is used to send emails. It is separate from Google OAuth
sign-in.

### 3.1 Create a Gmail App Password

1.  Sign in to the Google Account that will send email.
2.  Open [Google Account
    Security](https://myaccount.google.com/security).
3.  Enable **2-Step Verification** if it is not already enabled.
4.  Open [Google App
    Passwords](https://myaccount.google.com/apppasswords).
5.  Create an app password (Google may ask you to sign in again).
6.  Copy the generated password and store it securely.

App Passwords may not be available for some accounts, including certain
organization-managed accounts or accounts whose security configuration
disallows them. Do **not** use your normal Gmail account password in the
application.

### 3.2 Gmail SMTP settings

  Setting          Value
  ---------------- --------------------
  SMTP host        `smtp.gmail.com`
  Port             `587`
  Authentication   Enabled
  TLS              STARTTLS
  Username         Full Gmail address
  Password         Gmail App Password

Example Spring Mail configuration:

``` properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Set the environment variables for local development in PowerShell:

``` powershell
$env:SMTP_USERNAME="your-address@gmail.com"
$env:SMTP_PASSWORD="YOUR_GMAIL_APP_PASSWORD"
```

These example variables configure one SMTP account for the application.
If the application is later changed to support **each user entering
their own SMTP credentials**, store each user's settings separately,
associate them with the authenticated account on the server, encrypt
stored passwords, and never return passwords to the frontend. Google
Sign-In does not supply the user's SMTP password.

## 4. Run the application locally

### Backend

1.  Configure the environment variables above.

2.  Run the Spring Boot application from IntelliJ or with Maven:

    ``` bash
    mvn spring-boot:run
    ```

3.  The backend is expected to run at:

    ``` text
    http://localhost:8080
    ```

### Frontend

1.  Open a terminal in the React/Vite frontend directory.

2.  Install dependencies and start the development server:

    ``` bash
    npm install
    npm run dev
    ```

3.  The frontend is expected to run at:

    ``` text
    http://localhost:5173
    ```

If your project uses a different frontend tool or port, follow its
`package.json`.

## 5. Before deployment

-   Set Google OAuth and SMTP credentials as environment variables in
    the hosting platform.
-   Update the Google OAuth authorized redirect URI to the deployed
    backend callback URL.
-   Update the frontend API base URL to the deployed backend URL.
-   Allow the deployed frontend origin in Spring Security CORS
    configuration.
-   Ensure production redirects use HTTPS.
-   Configure session cookies appropriately for cross-site
    frontend/backend deployments if using cookie-based authentication.
-   Never expose secrets in frontend code, commit them to Git, or
    include them in screenshots/logs.
-   If a secret has already been committed or shared publicly, revoke or
    rotate it; deleting it from the latest file alone is not sufficient.
-   Test Google login, logout, Excel upload, and sending a test email
    after deployment.

## Troubleshooting

-   **`redirect_uri_mismatch`:** The callback URL in Google Cloud does
    not exactly match the callback used by Spring Security.
-   **Google login does not start:** Check that the backend is running
    and `/oauth2/authorization/google` is reachable.
-   **Gmail authentication fails:** Confirm the username is the full
    Gmail address and the password is an App Password, not the regular
    account password.
-   **CORS errors:** Confirm the exact frontend origin is allowed by the
    backend. Credentialed requests must not use wildcard origins.
-   **Email sender address:** With SMTP, recipients generally see the
    configured SMTP account as the sender. Google Sign-In alone does not
    send email from the signed-in user's account.

## Security reminder

Never ask users for their regular Google password. Do not log or display
SMTP App Passwords or OAuth Client Secrets. Validate recipient lists and
apply reasonable sending limits.
