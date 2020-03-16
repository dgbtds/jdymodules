package ecovacs.user;

import ecovacs.cache.CacheService;
import ecovacs.common.CommonService;
import ecovacs.dao.model.ResultModel;
import ecovacs.dao.pojoRepository.*;
import ecovacs.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private CustomerFollowRepository customerFollowRepository;

    @Autowired
    private AiCustomerRepository aiCustomerRepository;

    @Autowired
    private AiUserRepository aiUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommonService commonService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private CustomerVisitRepository customerVisitRepository;

    @Transactional
    @Override
    public ResultModel setCustomer(AiCustomer aiCustomer, Long aiUserId) {
        ResultModel resultModel = new ResultModel(0);
        AiUser aiUser = aiUserRepository.findByUserId(aiUserId);
        if(aiUser==null){
            return new ResultModel(1003,"无此销售顾问");
        }
        Long companyId = aiUser.getCompanyId();

        String customerLogo = aiCustomer.getCustomerLogo();
        String path = customerLogo;

        AiCustomer aiCustomerCheck =aiCustomerRepository.findAiCustomerByCompanyIdAndMobile(companyId, aiCustomer.getMobile());
        Long acceptTimes = aiUser.getAcceptTimes();
        if (aiCustomerCheck==null) {
            aiCustomer.setCounselorId(aiUserId);
            aiCustomer.setVisitNum(1L);
            aiCustomer.setCompanyId(companyId);
            aiCustomer.setCounselorId(aiUserId);
            aiCustomer.setTime(new Timestamp(System.currentTimeMillis()));
            aiCustomer.setCustomerLogo(path);
            AiCustomer save = aiCustomerRepository.save(aiCustomer);
            //到访记录表
            CustomerVisit customerVisit=new CustomerVisit();
            customerVisit.setCompanyId(companyId);
            customerVisit.setWeekIndex(commonService.getWeekIndex());
            customerVisit.setTime(new Timestamp(System.currentTimeMillis()));
            customerVisit.setCustomerId(save.getId());
            customerVisit.setToUserId(aiUserId);
            customerVisit.setFromUserId(aiUserId);
            customerVisit.setStatus(2);
            customerVisitRepository.save(customerVisit);

            aiUser.setDistributeCount(aiUser.getDistributeCount()+1);
            aiUser.setAcceptTimes(acceptTimes-1);
            aiUser.setAccepterStatus(1L);
           aiUserRepository.save(aiUser);
           if(acceptTimes>1) {
               cacheService.backToWait(aiUserId,companyId,aiUser.getGroupId());
           }
            resultModel.setMessage("顾客首次来访");
        }
        else {
            aiCustomerCheck.setVisitNum(aiCustomerCheck.getVisitNum()+1);

            //到访记录表
            CustomerVisit customerVisit=new CustomerVisit();
            customerVisit.setCompanyId(companyId);
            customerVisit.setWeekIndex(commonService.getWeekIndex());
            customerVisit.setTime(new Timestamp(System.currentTimeMillis()));
            customerVisit.setCustomerId(aiCustomerCheck.getId());
            customerVisit.setFromUserId(aiCustomerCheck.getCounselorId());
            customerVisit.setToUserId(aiUserId);
            if(customerVisit.getFromUserId().longValue()!=customerVisit.getToUserId().longValue()){
                customerVisit.setStatus(1);//客户主动转交
                resultModel.setMessage("顾客"+aiCustomerCheck.getVisitNum()+"次来访,更换接待人");
            }
            else {
                resultModel.setMessage("顾客"+aiCustomerCheck.getVisitNum()+"次来访,没有更换接待人");
                customerVisit.setStatus(2);
            }
            customerVisitRepository.save(customerVisit);
            aiCustomerCheck.setCounselorId(aiUserId);
            aiCustomerCheck.setCustomerLogo(path);
            aiCustomerCheck.setTime(new Timestamp(System.currentTimeMillis()));
            aiCustomerRepository.save(aiCustomerCheck);

            if(acceptTimes>1) {
                cacheService.backToWait(aiUserId,companyId,aiUser.getGroupId());
            }
            aiUser.setAcceptTimes(acceptTimes-1);
            aiUser.setAccepterStatus(1L);
            aiUserRepository.save(aiUser);
        }

        return resultModel;
    }

    @Override
    public ResultModel getVisitReview(Long customerId, Long aiUserId) {
        Optional<AiCustomer> customer = aiCustomerRepository.findById(customerId);
        AiCustomer aiCustomer = customer.get();
        if(aiCustomer.getCounselorId().longValue()!=aiUserId.longValue()) {
            return new ResultModel(1003,"非名下顾客");
        }
        List<CustomerVisit> customerVisitList =
                customerVisitRepository.findCustomerVisitByCustomerIdAndStatusNotOrderByTime
                        (customerId,0);
        ResultModel resultModel = new ResultModel(0);
        resultModel.setData(customerVisitList);
        return  resultModel;
    }


    @Transactional
    @Override
    public ResultModel workStatus(Long uid, Integer status) {
        if(status>1||status<0){
            return new ResultModel(1003);
        }

        aiUserRepository.updateWorkStatus(uid,status);
        return new ResultModel(0);
    }

    @Override
    public ResultModel getWorkStatus(Long uid) {
        AiUser aiUser = aiUserRepository.findByUserId(uid);

        ResultModel resultModel = new ResultModel(0);
        Map<String,String> data = new HashMap<>();
        data.put("workStatus",aiUser.getWorkStatus() + "");
        resultModel.setData(data);
        return resultModel;
    }

    @Override
    public ResultModel getMyCustomer(Long aiUserId) {
        AiUser byUserId = aiUserRepository.findByUserId(aiUserId);
        if (byUserId==null){
            return new ResultModel(1003,"没有此销售人员");
        }
        List<AiCustomer> myCustomersList = aiCustomerRepository.findAiCustomersByCounselorId(aiUserId);

        ResultModel resultModel=new ResultModel(0);

        resultModel.setData(myCustomersList);

        return resultModel;
    }

    @Override
    public ResultModel getMyCustomerDetail(Long customerId, Long aiUserId) {

        Optional<AiCustomer> customer = aiCustomerRepository.findById(customerId);
        AiCustomer aiCustomer = customer.get();
        if(aiCustomer.getCounselorId().longValue()!=aiUserId) {
            return new ResultModel(1003,"非名下顾客");
        }

        ResultModel resultModel=new ResultModel(0);
        resultModel.setData(aiCustomer);

        return resultModel;
    }
    @Transactional
    @Override
    public ResultModel addCustomerRecord(Long customerId, Long aiUserId,String content) {
        Optional<AiCustomer> customer = aiCustomerRepository.findById(customerId);
        AiCustomer aiCustomer = customer.get();
        if(aiCustomer.getCounselorId().longValue()!=aiUserId.longValue()) {
            return new ResultModel(1003,"非名下顾客");
        }

        CustomerFollowUp customerFollowUp = new CustomerFollowUp();
        customerFollowUp.setContent(content);
        customerFollowUp.setCreateTime(new Timestamp(System.currentTimeMillis()));
        customerFollowUp.setCustomerId(customerId);
        customerFollowUp.setStatus(1);
        customerFollowRepository.save(customerFollowUp);
        ResultModel resultMode = new ResultModel(0);
        return resultMode;
    }

    @Override
    public ResultModel getCustomerRecord(Long customerId, Long aiUserId) {
        AiCustomer aiCustomer = aiCustomerRepository.getOne(customerId);
        if(aiCustomer.getCounselorId().longValue()!=aiUserId) {
            return new ResultModel(1003,"非名下顾客");
        }
        List<CustomerFollowUp> customerFollowUps = customerFollowRepository.findByCustomerIdAndStatus(customerId, 1);

        ResultModel resultMode = new ResultModel(0);

        resultMode.setData(customerFollowUps);
        return resultMode;
    }

    @Override
    public ResultModel getMyself(Long aiUserId) {
        AiUser byUserId = aiUserRepository.findByUserId(aiUserId);
        User user = userRepository.getOne(aiUserId);
        if (byUserId==null||user==null){
            return new ResultModel(1003,"销售未记录");
        }
        ResultModel resultModel = new ResultModel(0);
        resultModel.setData(user);
        resultModel.setData(byUserId);
        return resultModel;
    }
    public static String passEncode(String pass) throws Exception {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(pass);
        return encode;
    }
    @Override
    public ResultModel changePW(Long aiUserId, String newPassWord) throws Exception {
        User user = userRepository.getOne(aiUserId);
        if (user==null){
            return new ResultModel(1003,"销售未记录");
        }
        user.setPassWord(passEncode(newPassWord));
        userRepository.save(user);
        ResultModel resultModel = new ResultModel(0);
        return resultModel;
    }

    @Transactional
    @Override
    public ResultModel delCustomerRecord(Long customerId, Long aiUserId, Long orderId) {

        AiCustomer aiCustomer = aiCustomerRepository.getOne(customerId);
        if(aiCustomer.getCounselorId().longValue()!=aiUserId) {
            return new ResultModel(1003,"非名下顾客");
        }
            CustomerFollowUp followup = customerFollowRepository.getOne(orderId);
            followup.setStatus(0);
            customerFollowRepository.save(followup);
            return new ResultModel(0);

    }


}
