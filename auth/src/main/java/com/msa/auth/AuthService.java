package com.msa.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserServiceClient userServiceClient;

    public String getUserInfo(String userId){
        return userServiceClient.getUser(userId);
    }


    public String getAuth(String id) {
        return id+" "+getUserInfo("2");
    }
}
