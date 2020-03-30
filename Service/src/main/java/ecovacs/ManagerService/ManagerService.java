package ecovacs.ManagerService;


import ecovacs.dao.model.ResultModel;

public interface ManagerService {
    ResultModel getCustomerList(Long userId);
    ResultModel getGroupbyCompany(Long userId);
    ResultModel setworkStatus(Long managerId, Long AiuserId, Integer status);
    ResultModel getGroupbyCompanyAndGroupId(Long userId, int groupid);
    ResultModel deleteGroup(Long userId, int groupid);
    ResultModel changeGroup(Long userId, Long AiuserId, int groupid);
    ResultModel changeGrouplist(Long userId, Long[] AiuserIds, int groupid);
    ResultModel setIndex(Long userId, Long AiuserId, Long index);
    ResultModel setTimes(Long userId, Long AiuserId, Long times);
    ResultModel setTimesList(Long userId, Long[] AiuserIds, Long[] timesList);
    ResultModel setIndexlist(Long userId, Long[] AiuserIdlist, Long[] indexList);
    ResultModel getPassHistory(Long managerId);
    ResultModel transferAll(Long managerId, Long fromAiuserId, Long toAiuserId);

    ResultModel getMyself(Long managerId, Long aiUserId);

    ResultModel getPriRecord( Long managerId, Long aiUserId);

    ResultModel register(Long managerId, String mobile, String passwd, String role, String name) ;

    ResultModel resetRedis(Long managerId);

    ResultModel setGrace(Long managerId, Long aiUserId, Long score);

    ResultModel registerALL(Long managerId, String[] mobile, String[] name);

    ResultModel transferOne(Long managerId, Long fromAiUserId, Long toaiUserId,Long customerId);

    ResultModel getCustomerDetail(Long customerId, Long managerId);
}
