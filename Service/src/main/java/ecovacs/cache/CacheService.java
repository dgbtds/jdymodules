package ecovacs.cache;


import ecovacs.dao.model.ResultModel;

import java.util.List;

public interface CacheService {


    void setAccepterGroupByCompanyId(Long companyId);
    List<Long> getAcceptersBycompanyIdAndGroupId(Long companyId, int count);
    void backToWait(Long aiUserId, Long companyId, int groupId);
    void choseOne(Long aiUserId, Long companyId, int groupId);
    void addAccepter(Long aiUserId, Long companyId, int groupId);
    void delAccepter(Long aiUserId, Long companyId, int groupId);
    void delGroup( Long companyId, int groupId);
    boolean haseWorkGroup(Long companyId);
     void resetAccepters(Long companyId);
    void groupRecover(Long companyId);
    void choseOne2(Long aiUserId, Long companyId, int groupId, Long[] aiUserIds);
}
