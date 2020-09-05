package com.changgou.oauth;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

public class ParseJwtTest {

    //基于公钥解析jwt
    @Test
    public void parseJwt(){

        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoibHliIiwiY29tcGFueSI6IkxZQiJ9.iY83fAno6ahY1jSmfPn4OTkCYbrvVFsX5CtsZWEHOnS3t5g0reFGffK8AcUFt1MeZb20gl3MwVU-2nk4voKvN-IeV2vh61AKS9COvMpW3B9F0GSKkU2HUp5bXV4it7KyLRk-aJRdTaYA8inGt0_v0N26aaNKiQSjHnNC2AebJ2ElVHzOYrkvUjUqj7Ft68qLmV3dy2nn5ZiAl6D0GtG4YzqvJ2FMAljgby_y9IB7ytuw4ATPn9M74PBNLmn_k_TUxPmt76S008vX3nupK06fZeucjc8CNmC3U_4I6kE9Qwiv1dJRmXKC49PPwMPKQH9dVuekbqLNydlG6OIjWhtbWA";
        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvFsEiaLvij9C1Mz+oyAmt47whAaRkRu/8kePM+X8760UGU0RMwGti6Z9y3LQ0RvK6I0brXmbGB/RsN38PVnhcP8ZfxGUH26kX0RK+tlrxcrG+HkPYOH4XPAL8Q1lu1n9x3tLcIPxq8ZZtuIyKYEmoLKyMsvTviG5flTpDprT25unWgE4md1kthRWXOnfWHATVY7Y/r4obiOL1mS5bEa/iNKotQNnvIAKtjBM4RlIDWMa6dmz+lHtLtqDD2LF1qwoiSIHI75LQZ/CNYaHCfZSxtOydpNKq8eb1/PGiLNolD4La2zf0/1dlcr5mkesV570NxRmU1tFm8Zd3MZlZmyv9QIDAQAB-----END PUBLIC KEY-----";

        //解析
        Jwt token = JwtHelper.decodeAndVerify(jwt, new RsaVerifier(publicKey));
        //获取token中内容
        String claims = token.getClaims();
        System.out.println(claims); //得到的结果就是一个json串对应我们在jwt中添加的内容{"name":"lyb","company":"LYB"}
    }
}
