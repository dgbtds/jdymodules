package ecovacs.controller.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecovacs.cache.CacheService;
import ecovacs.cache.CacheServiceImpl;
import ecovacs.config.WebSecurityConfig;
import ecovacs.dao.pojoRepository.UserRepository;
import ecovacs.pojo.AiUser;
import ecovacs.pojo.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * @author WuYe
 * @vesion 1.0 2019/11/14
 * /
 * /**
 * @program: parent
 * @description:
 * @author: WuYe
 * @create: 2019-11-14 15:36
 **/

public class ManagerControllerTest extends BaseTest{
    @Autowired
    private CacheServiceImpl cacheService;
    private String url="/user/go";
    private MockHttpServletRequestBuilder param;
    @Autowired
    private WebSecurityConfig wsc;
    @Before
    public void constructRep() throws Exception {
        User user=new User();
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"name\":\"wuye\", \"pass\":\"1\", \"mobile\":\"1234567\"}";
       // User user1 = mapper.readValue(jsonString, User.class);
         param = MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
              //  .content(mapper.writeValueAsString(user1))
                .param("mobile","1")
                .param("pass","1");

    }

    @Test
    public void register() throws Exception {
        mvc.perform(param)
                .andDo(print()) ;
    }

    @Test
    public void getPriRecord() {
        cacheService.setAccepterGroupByCompanyId(25L);
    }

    @Test
    public void transfer() {
        for(int i=633;i<=662;i++){
            User one = userRepository.findById(Long.parseLong(String.valueOf(i))).get();
            AiUser byUserId = aiUserRepository.findByUserId(one.getId());
            String mobile = one.getMobile();
            one.setMobile(one.getName());
            one.setName(mobile);
            byUserId.setName(mobile);
            aiUserRepository.save(byUserId);
            userRepository.save(one);
        }
    }

    @Test
    public void delGroup() {

    }

    @Test
    public void changeGroup() {
    }

    @Test
    public void changeGroups() {
    }

    @Test
    public void setIndex() {
    }

    @Test
    public void setIndexList() {
    }

    @Test
    public void setAcceptTimes() {
    }

    @Test
    public void setAcceptsTimes() {
    }

    @Test
    public void getGroupbyCompanyAndgroupId() {
    }

    @Test
    public void getGroupbyCompany() {
    }

    @Test
    public void getCustomerList() {
    }

    @Test
    public void setWorkStatus() {
    }

    @Test
    public void getPassHistory() {
    }
}
