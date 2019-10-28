package ecovacs.customer;


import ecovacs.dao.model.ResultModel;

public interface CustomerService {

    ResultModel choseone(Long aiUserId, String customerLogo);
    ResultModel firstVisit(String robotId);


    ResultModel uploadLogo(Long customerId, String logoPath);

}