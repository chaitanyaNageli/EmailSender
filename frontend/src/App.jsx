
import "./App.css";
import Login from "./login";
import { useEffect, useState } from "react";
import Profile from "./profile.jsx";

function App() {
    const [file, setFile] = useState(null);
    const [fileError, setFileError] = useState("");
    const [attachments, setAttachments] = useState([]);
    const [subject, setSubject] = useState("");
    const [body, setBody] = useState("");
    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState({});
    const [results, setResults] = useState([]);
    const [selectedStatus, setSelectedStatus] = useState("total");
    const [user, setUser] = useState(null);

    useEffect(() => {
        fetch("http://localhost:8080/api/auth/me", {
            credentials: "include",
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Unable to fetch user details");
                }
                return response.json();
            })
            .then((data) => {
                setUser(data);
            })
            .catch((error) => {
                console.error(error);
                setUser(null);
            });
    }, []);

    const handleFileChange = (selectedFile) => {
        setFileError("");

        if (!selectedFile) {
            setFile(null);
            return;
        }

        const extension = selectedFile.name
            .toLowerCase()
            .split(".")
            .at(-1);

        if (extension === "xlsx" || extension === "xls") {
            setFile(selectedFile);
        } else {
            setFile(null);
            setFileError("Please upload a valid .xlsx or .xls file.");
        }
    };

    const handleAttachmentChange = (e) => {
        setAttachments(Array.from(e.target.files || []));
    };

    const getCsrfToken = async () => {
        const response = await fetch("http://localhost:8080/api/auth/csrf", {
            method: "GET",
            credentials: "include",
        });

        if (!response.ok) {
            throw new Error("CSRF request failed: " + response.status);
        }

        const csrfData = await response.json();
        if (!csrfData.token) {
            throw new Error("CSRF token missing from response.");
        }

        return csrfData.token;
    };

    const handleSend = async () => {
        if (!file) {
            setFileError("Please upload an Excel file first.");
            return;
        }

        if (!subject.trim() || !body.trim()) {
            alert("Please enter both subject and message.");
            return;
        }

        const formData = new FormData();
        formData.append("file", file);
        formData.append("subject", subject);
        formData.append("body", body);

        attachments.forEach((attachment) => {
            formData.append("attachments", attachment);
        });

        try {
            setLoading(true);
            const csrfToken = await getCsrfToken();
            const response = await fetch("http://localhost:8080/api/emails/upload", {
                method: "POST",
                body: formData,
                credentials: "include",
                headers: {
                    "X-XSRF-TOKEN": csrfToken,
                },
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result?.message || result || "Failed to send emails.");
            }

            setStatus(result);
            setResults(result.results || []);
            setSelectedStatus("total");
        } catch (error) {
            setStatus({
                error: error.message || "Could not connect to the backend.",
            });
        } finally {
            setLoading(false);
        }
    };

    const handleEmailStatus = (option) => {
        setSelectedStatus(option);

        if (option === "total") {
            setResults(status.results || []);
            return;
        }

        const data = (status.results || []).filter(
            (item) => item.status.toLowerCase() === option
        );

        setResults(data);
    };

    const handleLogout = async () => {
        try {
            const csrfToken = await getCsrfToken();
            const logoutResponse = await fetch("http://localhost:8080/logout", {
                method: "POST",
                credentials: "include",
                headers: {
                    "X-XSRF-TOKEN": csrfToken,
                },
            });

            if (logoutResponse.status === 204) {
                setUser(null);
                window.location.href = "http://localhost:5173";
                return;
            }

            throw new Error("Logout failed: " + logoutResponse.status);
        } catch (error) {
            console.error("Logout error:", error);
            alert(error.message);
        }
    };

    if (!user) {
        return <Login />;
    }

    return (
        <div className="home">
            <div>
                <Profile user={user} onLogout={handleLogout} />
            </div>

            <header className="header">
                <h1>Bulk Email Sender</h1>
                <h3>Send personalized emails to multiple recipients.</h3>
            </header>

            <div className="container">
                <section className="email-panel">
                    <h2>Emails</h2>
                    <section className="emails-upload">
                        <h2>Upload Emails</h2>

                        <input
                            type="file"
                            id="file-upload"
                            className="file-upload"
                            accept=".xlsx,.xls"
                            onChange={(e) => handleFileChange(e.target.files?.[0])}
                        />

                        <label htmlFor="file-upload" className="file-label">
                            <span className="upload-icon">↑</span>
                            <strong>Choose Excel File</strong>
                            <span className="upload-hint">Supported formats: .xlsx, .xls</span>
                        </label>

                        {file && <p className="selected-file">✓ Selected: {file.name}</p>}
                        {fileError && <p className="file-error">{fileError}</p>}
                    </section>

                    <section className="email">
                        <h2>Email Details</h2>

                        <label htmlFor="subject">Subject</label>
                        <input
                            id="subject"
                            type="text"
                            placeholder="Enter email subject"
                            value={subject}
                            onChange={(e) => setSubject(e.target.value)}
                        />

                        <label htmlFor="message">Message</label>
                        <textarea
                            id="message"
                            placeholder="Write your email message here..."
                            value={body}
                            onChange={(e) => setBody(e.target.value)}
                        />
                    </section>
                </section>

                <aside className="right-panel">
                    <h2>Attachments</h2>
                    <p className="panel-description">
                        Add files to include with every email.
                    </p>

                    <input
                        type="file"
                        id="attachment-upload"
                        className="file-upload"
                        multiple
                        onChange={handleAttachmentChange}
                    />

                    <label htmlFor="attachment-upload" className="attachment-dropzone">
                        <span className="attachment-icon">＋</span>
                        <strong>Choose attachments</strong>
                        <span className="upload-hint">Select one or more files</span>
                    </label>

                    {attachments.length > 0 && (
                        <div className="attachment-list">
                            <h3>Selected files ({attachments.length})</h3>

                            {attachments.map((attachment, index) => (
                                <div className="attachment-item" key={`${attachment.name}-${index}`}>
                                    <span className="attachment-file-icon">▤</span>

                                    <div className="attachment-info">
                                        <span className="attachment-name">{attachment.name}</span>
                                        <span className="attachment-size">
                                            {(attachment.size / 1024).toFixed(1)} KB
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}

                </aside>

                <section className="status-panel">
                    <h2>Email Report</h2>
                    {status.error ? (
                        <p className="report-error">{status.error}</p>
                    ) : status.total !== undefined ? (
                        <>
                            <div className="email-status">
                                <p>Total: {status.total}</p>
                                <p>Sent: {status.sent}</p>
                                <p>Failed: {status.failed}</p>
                            </div>

                            <div className="email-results">
                                <select
                                    className="select-status"
                                    value={selectedStatus}
                                    onChange={(e) => handleEmailStatus(e.target.value)}
                                >
                                    <option value="total">Total</option>
                                    <option value="sent">Sent</option>
                                    <option value="failed">Failed</option>
                                </select>
                                {results?.map((item, index) => (
                                    <div key={`${item.email}-${index}`}>
                                        <strong>{item.email}</strong>
                                        <span className={item.status === "SENT" ? "sent" : "failed"}>
                                            {item.status}
                                        </span>
                                        <p>{item.message}</p>
                                    </div>
                                ))}
                            </div>
                        </>
                    ) : (
                        <p className="report-empty">Send emails to see the delivery report.</p>
                    )}
                </section>
            </div>

            <footer className="footer">
                <button type="button" onClick={handleSend} disabled={loading}>
                    {loading ? "Sending Emails..." : "Send Emails"}
                </button>
            </footer>
        </div>
    );
}

export default App;
