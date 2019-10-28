package ecovacs.cache;

import ecovacs.Util.reidsUtils.Redis;
import ecovacs.dao.pojoRepository.AiUserRepository;
import ecovacs.pojo.AiUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    private Redis redisRepository;
    @Autowired
    private AiUserRepository aiUserRepository;
    private  String WaitG;
    private  String WorkG;
    private  String ValueG;


    @Override
    public void setAccepterGroupByCompanyId(Long companyId) {

        List<AiUser> aiUsers = aiUserRepository.findAiUsersByCompanyId(companyId);
        Map<Integer, List<AiUser>> collect = aiUsers.parallelStream().filter(c->(c.getAcceptTimes()>0)&&(c.getSortIndex()!=null)).collect(
                Collectors.groupingBy(AiUser::getGroupId
                        )
        );
        AtomicInteger groupnum= new AtomicInteger();
                redisRepository.delete("company_"+companyId+"_GroupValue_");
                //redis模糊删除最后的通配符必须是+“*”；
                    redisRepository.delete("company_"+companyId+"_GroupWait_"+"*");
        collect.forEach((k,v)->{
            if (k > 0) {
                v.stream().sorted(Comparator.comparing(AiUser::getSortIndex)).forEach(c->{
                    redisRepository.lRightPush("company_"+c.getCompanyId()+"_GroupWait_"+c.getGroupId(),c.getUserId().toString());
                        }
                );
                redisRepository.lRightPush("company_"+companyId+"_GroupValue_",k.toString());
            }
        });
    }
    @Override
    public List<Long> getAcceptersBycompanyIdAndGroupId(Long companyId, int count) {
        int groupId;
        if (!redisRepository.hasKey("company_"+companyId+"_GroupValue_")){
            setAccepterGroupByCompanyId(companyId);
        }
             groupId=Integer.parseInt(redisRepository.lLeftPop("company_"+companyId+"_GroupValue_"));
            redisRepository.lRightPush("company_"+companyId+"_GroupValue_",groupId+"");
        if (redisRepository.hasKey("company_"+companyId+"_GroupWait_"+groupId)) {
            Long len = redisRepository.lLen("company_" + companyId + "_GroupWait_" + groupId);
            if(len==0){
                return  null;
            }
            if(len.longValue()<count){
                count=len.intValue();
            }
            List<Long> longs = new ArrayList<>();
            for (int i=0;i<count;i++){
                Long aiUserId = Long.parseLong(redisRepository.lLeftPopAndLeftPush("company_" + companyId + "_GroupWait_" + groupId,
                        "company_" + companyId + "_GroupWork_" + groupId));
                longs.add(aiUserId);
            }
            for (int i=0;i<count;i++){
               redisRepository.lLeftPopAndLeftPush("company_"+companyId+"_GroupWork_"+groupId,"company_"+companyId+"_GroupWait_"+groupId);
            }
            return longs;
        }
        else {
            return null;
        }
    }

    @Override
    public void choseOne(Long aiUserId,Long companyId, int groupId) {
        redisRepository.lRemove("company_"+companyId+"_GroupWait_"+groupId,0,aiUserId.toString());

    }
    @Override
    public void backToWait(Long aiUserId,Long companyId, int groupId) {
        redisRepository.lRightPush("company_"+companyId+"_GroupWait_"+groupId,aiUserId.toString());

    }


}
