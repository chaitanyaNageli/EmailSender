
import "./login.css";

export default function Login() {
    const handleGoogleLogin = () => {
        console.log("Google login button clicked");

        window.location.href =
            "http://localhost:8080/oauth2/authorization/google";
    };

    return (
        <div className="login-page">
            <div className="login-card">
                <div className="login-icon">✉</div>

                <h1>Bulk Email Sender</h1>

                <p className="login-description">
                    Send emails to multiple recipients with ease.
                </p>

                <div className="login-divider"></div>

                <h2>Welcome back</h2>

                <p className="login-subtitle">
                    Sign in with your Google account to continue.
                </p>

                <button
                    type="button"
                    className="google-login-btn"
                    onClick={handleGoogleLogin}
                >
                    <span className="google-g">G</span>
                    Continue with Google
                </button>

                <p className="login-footer">
                    Secure sign-in powered by Google
                </p>
            </div>
        </div>
    );
}
