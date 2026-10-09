package com.example.valtrak.UI.net;

import com.example.valtrak.Data.GameData.DataTransfer.AccountData.AccountDtos.LoginResponse;
import com.example.valtrak.Data.GameData.DataTransfer.AccountData.AccountDtos.ProfileDto;

/**
 * Who is signed in on this client, if anyone. Playing as a guest is the default. With "stay signed in", the token is
 * also saved on this computer ({@link SavedSession}) and checked with the server when the app starts.
 */
public final class AccountSession {
    private static String token;
    private static ProfileDto profile;
    private static volatile boolean restoring;

    private AccountSession() {}

    /** @param remember keep the player signed in after the app is closed */
    public static void signIn(LoginResponse login, boolean remember) {
        token = login.token();
        profile = login.profile();
        if (remember) SavedSession.save(ServerApi.BASE_URL, token);
        else SavedSession.clear();
    }

    /** True while a saved sign-in is being checked with the server at start-up. */
    public static boolean isRestoring() { return restoring; }

    public static void setRestoring(boolean value) { restoring = value; }

    private static volatile boolean restoreOffline;

    /** True if a saved sign-in couldn't be checked because the server was unreachable (it is kept for next time). */
    public static boolean restoreWasOffline() { return restoreOffline; }

    public static void setRestoreOffline(boolean value) { restoreOffline = value; }

    public static void updateProfile(ProfileDto updated) {
        profile = updated;
    }

    /** Signs out on this computer too (the saved sign-in is deleted). */
    public static void signOut() {
        token = null;
        profile = null;
        SavedSession.clear();
    }

    public static boolean isSignedIn() { return token != null; }

    public static String token() { return token; }

    public static ProfileDto profile() { return profile; }
}
