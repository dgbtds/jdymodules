package ecovacs.controller.manager;

import ecovacs.dao.pojoRepository.AiUserRepository;
import ecovacs.dao.pojoRepository.UserRepository;
import ecovacs.pojo.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author WuYe
 * @vesion 1.0 2019/11/14
 * /
 * /**
 * @program: parent
 * @description:
 * @author: WuYe
 * @create: 2019-11-14 15:41
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
public class BaseTest {
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected AiUserRepository aiUserRepository;
    protected MockMvc mvc;
    @Autowired
    private WebApplicationContext context;

    @Before
    public void setupMockMvc() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    @Test
    public void test1(){
        System.out.println("1111111");
    }
}
