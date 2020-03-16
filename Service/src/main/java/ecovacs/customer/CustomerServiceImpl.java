package ecovacs.customer;


import com.alibaba.fastjson.JSONObject;
import ecovacs.Util.JPush.JPushConfig;
import ecovacs.Util.NonUtil;
import ecovacs.cache.CacheService;
import ecovacs.common.CommonService;
import ecovacs.dao.model.ResultModel;
import ecovacs.dao.pojoRepository.*;
import ecovacs.pojo.AiUser;
import ecovacs.pojo.RobotRecord;
import ecovacs.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;

@SuppressWarnings("ALL")
@Service
public class CustomerServiceImpl implements CustomerService {

    @Value("${base.path}")
    private String basepath;
    @Autowired
    private AiCustomerRepository aiCustomerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerFollowRepository customerFollowRepository;

    @Autowired
    private CustomerVisitRepository customerVisitRepository;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private AiUserRepository aiUserRepository;



    @Autowired
    private RobotRecordRepository robotRecordRepository;

//        messageMap.put(0,"success");
//        messageMap.put(1000,"服务器处理失败");
//        messageMap.put(1001,"权限不足");
//        messageMap.put(1002,"帐号或密码不正确");
//        messageMap.put(1003,"参数不正确");
//        messageMap.put(1004,"帐号不存在");



    @Override
    public ResultModel firstVisit(String robotId) {
        if(NonUtil.isNon(robotId)){
            return new ResultModel(1003,"机器人id不存在");
        }
        ResultModel resultModel = new ResultModel(0);

        //1.通过sn获取机器人,通过robotrecord获取userId
        RobotRecord robotRecord = robotRecordRepository.findBySn(robotId);
        if(NonUtil.isNon(robotRecord)) {
            return new ResultModel(1,"所传机器人sn有误");
        }
        Long companyId = robotRecord.getCompanyId();


        User manager = userRepository.getOne(robotRecord.getUserId());
        Long managerId=manager.getId();

            if(!cacheService.haseWorkGroup(companyId)){
                cacheService.resetAccepters(companyId);
                    if(!cacheService.haseWorkGroup(companyId)) {
                        return new ResultModel(4,"当前公司没有工作组,测试问题！！！！！");
                    }
            }
        List<AiUser> recommendlist=new ArrayList<>();
        List<Long> recommendId;
        do {
            recommendId=cacheService.getAcceptersBycompanyIdAndGroupId(companyId,3);
            if (recommendId==null){
                cacheService.setAccepterGroupByCompanyId(companyId);
                return new ResultModel(5,"数据库错误,已重置");
            }
            }while (recommendId.size()==0);

        for (Long aiUserId:recommendId){
            AiUser aiUser = aiUserRepository.findByUserId(aiUserId);
            aiUser.setAccepterStatus(0L);
            aiUserRepository.save(aiUser);
            recommendlist.add(aiUser);
        }


        resultModel.setData(recommendlist);
      //  simpMessagingTemplate.convertAndSend("/com.jdy.client.controller.customer/"+robotId,map);//发送给机器人端口

        return resultModel;
    }
    @Override
    public ResultModel choseone(Long aiUserId,String customerLogo){
    if(NonUtil.isNon(aiUserId,customerLogo)){
        return new ResultModel(1003);
    }
        AiUser aiUser = aiUserRepository.findByUserId(aiUserId);
        Long companyId = aiUser.getCompanyId();
        aiUser.setAccepterStatus(0L);
        aiUserRepository.save(aiUser);

        String prefixName = basepath+File.separator+"img"+File.separator;
        ResultModel resultModel = new ResultModel(0);
    Map<String,Object> map1 = new HashMap<>();
        String path = prefixName +"company_"+ companyId+ File.separator+"Customer"+ File.separator +customerLogo;
    map1.put("customerLogo",path);
    //发送到手机,信息
    sendMessage(JSONObject.toJSONString(map1),aiUserId.toString());
    cacheService.choseOne(aiUserId,aiUser.getCompanyId(),aiUser.getGroupId());

//    Map<String,Object> map2=new HashMap<>();
//    resultModel.setData(map2);

    return resultModel;
}


    public void sendMessage(String content,String liase) {

        JPushConfig.send0(content,liase);

    }

    @Transactional
    @Override
    public ResultModel uploadLogo(Long customerId, String logoPath) {
        ResultModel resultModel = new ResultModel(0);
        aiCustomerRepository.updateLogo(customerId,logoPath);
        return resultModel;
    }

    @Override
    public ResultModel choseone2(Long aiUserId, String customerLogo, Long[] aiUserIds) {
        if (aiUserId==-1){
            AiUser aiUser2=null;
            for (Long id:aiUserIds){
                    aiUser2 = aiUserRepository.findByUserId(id);
                    if (aiUser2==null){
                        return new ResultModel(1003,"客服id无此人");
                    }
                    aiUser2.setAccepterStatus(1L);
                    aiUserRepository.save(aiUser2);
            }
            cacheService.groupRecover(aiUser2.getCompanyId());
            cacheService.choseOne2(aiUserId,aiUser2.getCompanyId(),aiUser2.getGroupId(),aiUserIds);
            return new ResultModel(0);
        }
        if(NonUtil.isNon(aiUserId,customerLogo)){
            return new ResultModel(1003);
        }
        if(aiUserIds.length==0){
            return new ResultModel(1003);
        }
        AiUser aiUser = aiUserRepository.findByUserId(aiUserId);
        Long companyId = aiUser.getCompanyId();
        aiUser.setAccepterStatus(0L);
        aiUserRepository.save(aiUser);

        String prefixName = basepath+File.separator+"img"+File.separator;
        ResultModel resultModel = new ResultModel(0);
        Map<String,Object> map1 = new HashMap<>();
        String path = prefixName +"company_"+ companyId+ File.separator+"Customer"+ File.separator +customerLogo;
        map1.put("customerLogo",path);
        for (Long id:aiUserIds){
            if (aiUserId.longValue()!=id.longValue()) {
                AiUser aiUser2 = aiUserRepository.findByUserId(id);
                aiUser2.setAccepterStatus(1L);
                 aiUserRepository.save(aiUser2);
            }
        }
        //发送到手机,信息
        sendMessage(JSONObject.toJSONString(map1),aiUserId.toString());
        cacheService.choseOne2(aiUserId,aiUser.getCompanyId(),aiUser.getGroupId(),aiUserIds);
        return resultModel;
    }




}
