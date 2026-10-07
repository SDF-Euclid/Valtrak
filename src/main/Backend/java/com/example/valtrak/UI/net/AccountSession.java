package com.example.valtrak.UI.net;

import com.example.valtrak.Data.GameData.DataTransfer.AccountData.AccountDtos.LoginResponse;
import com.example.valtrak.Data.GameData.DataTransfer.AccountData.AccountDtos.ProfileDto;

/**
 * Who is signed in on this client, if anyone. Playing as a guest is the default;
 * the token is kept in memory only, so the player signs in again on each launch.
 */
public final class AccountSession {
    private static String token;
    private static ProfileDto profile;

    private AccountSession() {}

    public static void signIn(LoginResponse login) {
        token = login.token();
        profile = login.profile();
    }

    public static void updateProfile(ProfileDto updated) {
        profile = updated;
    }

    public static void signOut() {
        token = null;
        profile = null;
    }

    public static boolean isSignedIn() { return token != null; }

    public static String token() { return token; }

    public static ProfileDto profile() { return profile; }
}
