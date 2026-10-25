
import { useState } from "react";
import "./App.css";

export default function Profile({ user, onLogout }) {
    const [seeMenu, setSeeMenu] = useState(false);

    const handleProfile = () => {
        setSeeMenu((prev) => !prev);
    };

    if (!user) {
        return null;
    }

    return (
        <div className="profile">
            <button className="profile-pic" onClick={handleProfile}>
                <img className="profile-pic" src={user.picture} alt="Profile" />
            </button>

            {seeMenu && (
                <div className="profile-info">
                    <p>{user?.name || "Loading name..."}</p>
                    <p>{user?.email || "Loading email..."}</p>

                    <button className="logout" onClick={onLogout}>
                        Logout
                    </button>
                </div>
            )}
        </div>
    );
}
