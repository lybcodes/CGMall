package com.changgou.test;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class TestBcrypt {
    public static void main(String[] args) {
        String gensalt = BCrypt.gensalt();//这个是盐值  29个字符，随机生成
        String hashpw = BCrypt.hashpw("123456", gensalt);//根据盐对密码进行加密
        System.out.println(hashpw);

        //BCrypt不支持反运算，只支持密码校验
        boolean checkpw = BCrypt.checkpw("123456", "$2a$10$61ogZY7EXsMDWeVGQpDq3OBF1.phaUu7.xrwLyWFTOu8woE08zMIW");
        System.out.println(checkpw);
    }
}
