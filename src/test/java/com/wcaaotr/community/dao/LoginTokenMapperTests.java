package com.wcaaotr.community.dao;

import com.wcaaotr.community.CommunitydemoApplication;
import com.wcaaotr.community.entity.LoginToken;
import org.apache.kafka.common.security.authenticator.LoginManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Connor
 * @create 2021-06-30-22:05
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunitydemoApplication.class)
public class LoginTokenMapperTests {
    @Autowired(required = false)
    private LoginTokenMapper loginTokenMapper;

    @Test
    public void testUpdataLoginTokenStatus() {
        LoginToken token = loginTokenMapper.selectLoginTokenByToken("236c621804bd4ad997655e1ca3eae9d7");
        System.out.println(token);
        loginTokenMapper.updateLoginTokenStatus("236c621804bd4ad997655e1ca3eae9d7", 1);
    }
}
