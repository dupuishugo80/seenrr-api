package com.seenrr.seenrr.service;

import org.springframework.stereotype.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

@Service
public class TwoFactorAuthService {
    private final static GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public static GoogleAuthenticatorKey generateSecretKey() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key;
    }

    public static String getQrCodeUrl(String email, GoogleAuthenticatorKey secret) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL("Seenrr", email, secret);
    }

    public static boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}
