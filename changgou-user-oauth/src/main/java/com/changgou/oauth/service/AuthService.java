package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

public interface AuthService {
    AuthToken applyToken(String username, String password, String clientId, String clientSecret);
}
