package ecovacs.login;


import ecovacs.dao.model.ResultModel;

public interface GenericService {
    ResultModel login(String mobil, String pass);

    ResultModel getWeek(Long userId);
}
