package ecovacs.ManagerService;

import ecovacs.cache.CacheService;
import ecovacs.common.CommonService;
import ecovacs.dao.model.ResultModel;
import ecovacs.dao.pojoRepository.AiCustomerRepository;
import ecovacs.dao.pojoRepository.AiUserRepository;
import ecovacs.dao.pojoRepository.CustomerVisitRepository;
import ecovacs.dao.pojoRepository.UserRepository;
import ecovacs.pojo.AiCustomer;
import ecovacs.pojo.AiUser;
import ecovacs.pojo.CustomerVisit;
import ecovacs.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ManagerServiceImpl implements ManagerService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AiUserRepository aiUserRepository;
    @Autowired
    private CommonService commonService;
    @Autowired
    private AiCustomerRepository aiCustomerRepository;
    @Autowired
    private CustomerVisitRepository customerVisitRepository;
    @Autowired
    private CacheService cacheService;

    public static String passEncode(String pass) throws Exception {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(pass);
        return encode;
    }

    @Override
    public ResultModel getCustomerList(Long userId) {
        ResultModel resultModel=new ResultModel(0);
        Map<String,Object>map = new HashMap<String,Object>();

        User manager=userRepository.getOne(userId);
        Long companyId=manager.getCompanyId();
        List<AiCustomer> customers = aiCustomerRepository.findAiCustomersByCompanyId(companyId);
        map.put("cutomerSize",customers.size());
        map.put("customersList",customers);

        resultModel.setData(map);
        return resultModel;
    }

    @Override
    public ResultModel getGroupbyCompany(Long userId) {
        ResultModel resultModel=new ResultModel(0);
        Map<String ,Object>resultMap=new HashMap<>();
        User user = userRepository.getOne(userId);//获取不到自己不会报异常
        Long companyId = user.getCompanyId();
//        List<AiUser> aiUsers = aiUserRepository.findAiUsersByCompanyId(companyId);
//        Map<Integer, List<AiUser>> collect = aiUsers.parallelStream().collect(
//                Collectors.groupingBy(AiUser::getGroupId
//                )
//        );
        List<Map<String, Object>> list = aiUserRepository.countByCompanyIdGrAndGroupId(companyId);
        resultModel.setData(list);
        return resultModel;
    }

    @Override
    public ResultModel setworkStatus(Long managerId,Long AiuserId, Integer status) {
        getFromParams(managerId,AiuserId);
        AiUser byUserId = aiUserRepository.findByUserId(AiuserId);
        if (byUserId==null){
            return new ResultModel(1003);
        }
        byUserId.setWorkStatus(status);
        aiUserRepository.save(byUserId);
        return new ResultModel(0);
    }

    @Override
    public ResultModel getGroupbyCompanyAndGroupId(Long userId, int groupid) {
        ResultModel resultModel=new ResultModel(0);
        Map<String,Object> map=new HashMap<>();


        User user = userRepository.getOne(userId);//获取不到自己会报异常处理不用检测了
        Long companyId = user.getCompanyId();

        List<AiUser> list = aiUserRepository.findAiUsersByCompanyIdAndGroupIdOrderBySortIndexAsc(companyId,groupid);
        if (list.size()==0){
            return new ResultModel(5,"不存在此组号");
        }
        list.forEach(aiUser -> {
            Long aLong = aiCustomerRepository.countByCounselorId(aiUser.getUserId());
            aiUser.setCustomerNum(aLong);
        });
        resultModel.setData(list);

        return resultModel;
    }
    @Transactional
    @Override
    public ResultModel transferAll(Long managerId, Long fromAiuserId, Long toAiuserId) {
        User manager=userRepository.getOne(managerId);
        AiUser fromAiUser = aiUserRepository.findByUserId(fromAiuserId);
        AiUser toAIUser = aiUserRepository.findByUserId(toAiuserId);
        if(!(
                manager.getCompanyId().longValue()== fromAiUser.getCompanyId().longValue()&&
                        fromAiUser.getCompanyId().longValue()==toAIUser.getCompanyId().longValue()
        )
        ){
            return new ResultModel(1003,"非一家公司人员");
        }
        List<AiCustomer> Customers = aiCustomerRepository.findAiCustomersByCounselorId(fromAiuserId);
        Customers.forEach(
                c->{c.setCounselorId(toAiuserId);
                    aiCustomerRepository.save(c);
                    //到访记录表
                    CustomerVisit customerVisit=new CustomerVisit();
                    customerVisit.setCompanyId(fromAiUser.getCompanyId());
                    customerVisit.setWeekIndex(commonService.getWeekIndex());
                    customerVisit.setTime(new Timestamp(System.currentTimeMillis()));
                    customerVisit.setCustomerId(c.getId());
                    customerVisit.setToUserId(toAiuserId);
                    customerVisit.setFromUserId(fromAiuserId);
                    customerVisit.setStatus(0);
                    customerVisitRepository.save(customerVisit);
                });


        ResultModel resultModel=new ResultModel(0);
        Map<String,Object> map=new HashMap<>();
        map.put("toAiuserId",toAiuserId);
        map.put("fromAiuserId",fromAiuserId);
        map.put("managerId",managerId);
        map.put("passCustomerCount",Customers.size());

        resultModel.setData(map);

        return resultModel;
    }

    @Override
    public ResultModel getMyself(Long managerId, Long aiUserId) {
        Optional<User> user1 = userRepository.findById(aiUserId);
        Optional<User> manager1 = userRepository.findById(managerId);
        User user = user1.get();
        User manager = manager1.get();
        AiUser aiUser = aiUserRepository.findByUserId(aiUserId);
        if (user==null){
            return new ResultModel(1003,"人员未记录");
        }
        if (manager==null){
            return new ResultModel(1003,"无此管理");
        }
        if (aiUser==null){
            return new ResultModel(1003,"非销售人员");
        }
        if(user.getCompanyId().longValue()!=manager.getCompanyId().longValue()){
            return new ResultModel(1003,"公司不匹配");
        }
        ResultModel resultModel = new ResultModel(0);
        resultModel.setData(aiUser);
        return resultModel;
    }

    @Override
    public ResultModel getPriRecord( Long managerId, Long aiUserId) {
        Optional<User> user1 = userRepository.findById(aiUserId);
        Optional<User> manager1 = userRepository.findById(managerId);
        User user = user1.get();
        User manager = manager1.get();
        if (user==null){
            return new ResultModel(1003,"人员未记录");
        }
        if (manager==null){
            return new ResultModel(1003,"无此管理");
        }
        if(user.getCompanyId().longValue()!=manager.getCompanyId().longValue()){
            return new ResultModel(1003,"公司不匹配");
        }
        ResultModel resultModel = new ResultModel(0);
        resultModel.setData(user);
        return resultModel;
    }

    @Override
    @Transactional
    public ResultModel register(Long managerId, String mobile, String passwd, String role, String name)  {
        User one = userRepository.getOne(managerId);
        if (one==null){
            return new ResultModel(1003,"管理未注册");
        }
        UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(mobile, passwd);
        long roleId = Long.parseLong(role);
        if (roleId!=1007&&roleId!=1004){
            return new ResultModel(1003,"role= 1004||1007");
        }
        User user1 = userRepository.findByMobileAndStatus(mobile, 1);
        if (user1!=null){
            return new ResultModel(1003,"mobile exist");
        }
        User user = new User();
        user.setName(name);
        user.setMobile(mobile);
        user.setTime(new Timestamp(System.currentTimeMillis()));
        user.setStatus(1);
        try {
            user.setPassWord(passEncode(passwd));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultModel(1003,"加密失败");
        }
        user.setRoleId(roleId);
        user.setCompanyId(one.getCompanyId());
        User save = userRepository.save(user);
        if (roleId==1004){
            AiUser aiUser = new AiUser();
            aiUser.setName(name);
            aiUser.setGroupId(0);
            aiUser.setCompanyId(one.getCompanyId());
            aiUser.setUserId(save.getId());
            aiUser.setWorkStatus(0);
            aiUserRepository.save(aiUser);
        }
        ResultModel resultModel = new ResultModel(0);
        resultModel.setData(save);
        return resultModel;
    }

    @Override
    public ResultModel resetRedis(Long managerId) {
        Optional<User> byId = userRepository.findById(managerId);
        User user= byId.get();
        if (user==null){
            return new ResultModel(1003,"参数错误");
        }
        Long companyId = user.getCompanyId();
        cacheService.setAccepterGroupByCompanyId(companyId);
        return new ResultModel(0);
    }

    @Override
    public ResultModel setGrace(Long managerId, Long aiUserId, Long score) {
        getFromParams(managerId,aiUserId);
        AiUser byUserId = aiUserRepository.findByUserId(aiUserId);
        if (byUserId==null){
            return new ResultModel(1003);
        }
        byUserId.setScore(score);
        aiUserRepository.save(byUserId);
        return new ResultModel(0);
    }

    @Override
    public ResultModel registerALL(Long managerId, String[] mobile, String[] name) {
        User one = userRepository.getOne(managerId);
        if (one==null){
            return new ResultModel(1003,"管理未注册");
        }
        if(mobile.length!=name.length){
            return new ResultModel(1003,"数组长度不匹配");
        }
        int length=mobile.length;
        for(int i=0;i<length;i++){
            User user1 = userRepository.findByMobileAndStatus(mobile[i], 1);
            if (user1!=null){
                return new ResultModel(1003,"mobile exist,参数错误");
            }
            User user = new User();
            user.setName(name[i]);
            user.setMobile(mobile[i]);
            user.setTime(new Timestamp(System.currentTimeMillis()));
            user.setStatus(1);
            try {
                user.setPassWord(passEncode("1"));
            } catch (Exception e) {
                e.printStackTrace();
                return new ResultModel(1003,"加密失败");
            }
            user.setRoleId(1004L);
            user.setCompanyId(one.getCompanyId());
            User save = userRepository.save(user);

            AiUser aiUser = new AiUser();
            aiUser.setName(name[i]);
            aiUser.setGroupId(0);
            aiUser.setCompanyId(one.getCompanyId());
            aiUser.setUserId(save.getId());
            aiUser.setWorkStatus(0);
            aiUserRepository.save(aiUser);
        }
        ResultModel resultModel = new ResultModel(0);
        return resultModel;
    }

    @Override
    public ResultModel transferOne(Long managerId, Long fromAiuserId, Long toAiuserId,Long customerId) {

        User manager=userRepository.getOne(managerId);
        AiUser fromAiUser = aiUserRepository.findByUserId(fromAiuserId);
        AiUser toAIUser = aiUserRepository.findByUserId(toAiuserId);
        if(!(
                manager.getCompanyId().longValue()== fromAiUser.getCompanyId().longValue()&&
                        fromAiUser.getCompanyId().longValue()==toAIUser.getCompanyId().longValue()
        )
        ){
            return new ResultModel(1003,"非一家公司人员");
        }
        AiCustomer customer = aiCustomerRepository.getOne(customerId);
        if (customer==null||!(customerId.equals(fromAiuserId))){
            ResultModel resultModel0 = new ResultModel(1003);
            resultModel0.setMessage("没有此销售人员或客户非被转交销售名下");
            return resultModel0;
        }
        customer.setCounselorId(toAiuserId);
        aiCustomerRepository.save(customer);
        //到访记录表
        CustomerVisit customerVisit=new CustomerVisit();
        customerVisit.setCompanyId(fromAiUser.getCompanyId());
        customerVisit.setWeekIndex(commonService.getWeekIndex());
        customerVisit.setTime(new Timestamp(System.currentTimeMillis()));
        customerVisit.setCustomerId(customer.getId());
        customerVisit.setToUserId(toAiuserId);
        customerVisit.setFromUserId(fromAiuserId);
        customerVisit.setStatus(0);
        customerVisitRepository.save(customerVisit);


        Map<String,Object> map=new HashMap<>();
        map.put("toAiuserId",toAiuserId);
        map.put("fromAiuserId",fromAiuserId);
        map.put("managerId",managerId);
        map.put("CustomerId",customerId);

        ResultModel resultModel = new ResultModel(0);
        resultModel.setData(map);

        return resultModel;
    }


    @Transactional
    @Override
    public ResultModel deleteGroup(Long userId,int groupid) {
        User user = userRepository.getOne(userId);//findone返回optinal类型，.get（）得到本类型
        Long companyId = user.getCompanyId();
        List<AiUser> aiUsers = aiUserRepository.findAiUsersByCompanyIdAndGroupId(companyId,groupid);
        aiUsers.forEach(
                aiUser -> {
                    aiUser.setGroupId(0);
                    aiUserRepository.save(aiUser);
                }
        );
        cacheService.delGroup(companyId,groupid);
        ResultModel resultModel=new ResultModel(0);
        return resultModel;
    }
    @Transactional
    @Override
    public ResultModel changeGroup( Long managerId,Long aiUserId, int groupId) {
        ResultModel model = getFromParams(managerId, aiUserId);
        if (model.getStatus()!=0){
            return model;
        }
        Map<String,Object> mapclass= (Map<String, Object>) model.getData();
        AiUser aiUser= (AiUser) mapclass.get("aiuser");
        int oldGroupId = aiUser.getGroupId();
        if (oldGroupId==groupId){
            return new ResultModel(0);
        }
        Long companyId = aiUser.getCompanyId();


        aiUser.setGroupId(groupId);
        aiUser.setSortIndex(10L);
        aiUserRepository.save(aiUser);

        ResultModel resultModel=new ResultModel(0);
        cacheService.delAccepter(aiUserId,companyId,oldGroupId);
        cacheService.addAccepter(aiUserId,companyId,groupId);
        return resultModel;
    }

    @Override
    public ResultModel changeGrouplist(Long userId,Long[] AiuserIds, int groupid) {
        for(Long a:AiuserIds){
            ResultModel Model=changeGroup(userId,a,groupid);
        }
        return  new ResultModel(0);
    }
    @Transactional
    @Override
    public ResultModel setIndex(Long userId, Long AiuserId, Long index) {
        Map<String,Object> mapclass= (Map<String, Object>) getFromParams(userId,AiuserId).getData();
        AiUser aiUser= (AiUser) mapclass.get("aiuser");
        User user= (User) mapclass.get("com/jdy/client/controller/user");
        Long companyId = aiUser.getCompanyId();


        aiUser.setSortIndex(index);
        aiUserRepository.save(aiUser);

        ResultModel resultModel=new ResultModel(0);
        cacheService.setAccepterGroupByCompanyId(companyId);

        return resultModel;
    }
    @Override
    public ResultModel setIndexlist(Long userId, Long[] AiuserIds, Long[] indexs) {

        if(AiuserIds.length!=indexs.length){
            return new ResultModel(1003);
        }
        for(int i=0;i<indexs.length;i++){

            setIndex(userId,AiuserIds[i],indexs[i]);
        }
        return new ResultModel(0);
    }

    @Override
    public ResultModel getPassHistory(Long userId) {
        ResultModel resultModel=new ResultModel(0);
        Map<String,Object>map = new HashMap<String,Object>();

        User manager=userRepository.getOne(userId);
        Long companyId=manager.getCompanyId();
        List<CustomerVisit> customerPass = customerVisitRepository.findCustomerVisitByCustomerIdAndStatusNotOrderByTime(companyId, 3);
        resultModel.setData(customerPass);
        return resultModel;
    }

    @Transactional
    @Override
    public ResultModel setTimes(Long userId, Long AiuserId, Long times) {
        Map<String,Object>mapclass= (Map<String, Object>) getFromParams(userId,AiuserId).getData();
        AiUser aiUser= (AiUser) mapclass.get("aiuser");
        User user= (User) mapclass.get("com/jdy/client/controller/user");
        Long companyId= (Long) mapclass.get("companyId");
        Long oldtimes = aiUser.getAcceptTimes();

        if(times.longValue()<0){
            return new ResultModel(1003);
        }
        aiUser.setAcceptTimes(times);
        aiUserRepository.save(aiUser);
        ResultModel resultModel=new ResultModel(0);

        return resultModel;
    }

    @Override
    public ResultModel setTimesList(Long userId, Long[] AiuserIds, Long[] timesList) {
        if(AiuserIds.length!=timesList.length){
            return new ResultModel(1003);
        }
        return new ResultModel(0);
    }



    public ResultModel getFromParams(Long userId, Long AiuserId){
        User user = userRepository.getOne(userId);
        AiUser aiUser = aiUserRepository.findByUserId(AiuserId);
        User one = userRepository.getOne(AiuserId);
        if(user==null||aiUser==null){
            return new ResultModel(1003,"非销售人员");
        }
        Long companyId=user.getCompanyId();
        ResultModel resultModel=new ResultModel(0);
        if(aiUser.getCompanyId().longValue()!=user.getCompanyId().longValue()){
            return new ResultModel(1003,"公司不匹配");
        }
        Map<String,Object> map=new HashMap<>();
        map.put("user",user);
        map.put("one",one);
        map.put("aiuser",aiUser);
        map.put("companyId",companyId);
        resultModel.setData(map);
        return resultModel;
    }
}
