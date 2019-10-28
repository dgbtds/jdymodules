package ecovacs.user;

import ecovacs.cache.CacheService;
import ecovacs.common.CommonService;
import ecovacs.dao.model.ResultModel;
import ecovacs.dao.pojoRepository.AiCustomerRepository;
import ecovacs.dao.pojoRepository.AiUserRepository;
import ecovacs.dao.pojoRepository.CustomerFollowRepository;
import ecovacs.dao.pojoRepository.CustomerVisitRepository;
import ecovacs.pojo.AiCustomer;
import ecovacs.pojo.AiUser;
import ecovacs.pojo.CustomerFollowUp;
import ecovacs.pojo.CustomerVisit;
import org.springframework.beans.factory.annotation.Autowired;
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
        String prefixName = "/data"+ File.separator+"img"+File.separator;

        String customerLogo = aiCustomer.getCustomerLogo();
        String path = prefixName +"company_"+ companyId+ File.separator+"Customer"+ File.separator +customerLogo;

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
           aiUserRepository.save(aiUser);
            cacheService.backToWait(aiUserId,companyId,aiUser.getGroupId());
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

            cacheService.backToWait(aiUserId,companyId,aiUser.getGroupId());
            aiUser.setAcceptTimes(acceptTimes-1);
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
        List<AiCustomer> myCustomersList = aiCustomerRepository.findAiCustomersByCounselorId(aiUserId);

        ResultModel resultModel=new ResultModel(0);
//        Map<String,Object> map=new HashMap<>();
//        Map<Long, AiCustomer> collect = myCustomersList.stream().collect(Collectors.toMap(
//                AiCustomer::getId,
//                a -> a
//                , ((k1, k2) -> k1)
//                )
//        );
//        collect.forEach((k,v)->{
//                    Map<String,Object> simpleCustomerInfo=new HashMap<>();
//                    simpleCustomerInfo.put("customerId",v.getId());
//                    simpleCustomerInfo.put("customerName",v.getName());
//                    simpleCustomerInfo.put("customerMobile",v.getMobile());
//                    simpleCustomerInfo.put("customerLogo",v.getCustomerLogo());
//                    simpleCustomerInfo.put("customerVisiNum",v.getVisitNum());
//                    simpleCustomerInfo.put("customerLastVisiTime",v.getTime());
//                    map.put( String.valueOf(v.getId()),simpleCustomerInfo);
//                }
//        );

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
        CustomerFollowUp save = customerFollowRepository.save(customerFollowUp);
        ResultModel resultMode = new ResultModel(0);
        return new ResultModel(0);
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
        return new ResultModel(0);
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
