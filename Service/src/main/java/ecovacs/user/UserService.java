package ecovacs.user;


import ecovacs.dao.model.ResultModel;
import ecovacs.pojo.AiCustomer;

public interface UserService {

    ResultModel setCustomer(AiCustomer aiCustomer, Long aiUserId);

    ResultModel workStatus(Long uid, Integer status);

    ResultModel getWorkStatus(Long uid);

    ResultModel getMyCustomer(Long aiUserId);

    ResultModel getMyCustomerDetail(Long customerId, Long aiUserId);

    ResultModel addCustomerRecord(Long customerId, Long aiUserId, String content);

    ResultModel delCustomerRecord(Long customerId, Long aiUserId, Long orderId);

    ResultModel getVisitReview(Long customerId, Long aiUserId);

    ResultModel getCustomerRecord(Long customerId, Long aiUserId);

    ResultModel getMyself(Long aiUserId);

    ResultModel changePW(Long aiUserId, String newpassword) throws Exception;
}
