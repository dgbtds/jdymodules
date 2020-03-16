package ecovacs.cache;

import ecovacs.Util.reidsUtils.Redis;
import ecovacs.dao.pojoRepository.AiUserRepository;
import ecovacs.pojo.AiUser;
import org.hibernate.annotations.Check;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    public Redis redisRepository;
    @Autowired
    private AiUserRepository aiUserRepository;

    @Override
    public void resetAccepters(Long companyId) {
        List<AiUser> aiUsers = aiUserRepository.getGroupInfo(companyId);
        Map<Integer, List<AiUser>> collect = aiUsers.parallelStream().collect(
                Collectors.groupingBy(AiUser::getGroupId )
        );
        deleteALl(companyId);
        collect.forEach((k,v)->{
            if (k > 0) {
                v.stream().sorted(Comparator.comparing(AiUser::getSortIndex)).filter(c->c.getAccepterStatus()!=0).forEach(c->{
                            if (c.getAcceptTimes()==0) {
                                c.setAcceptTimes(3L);
                               aiUserRepository.save(c);
                            }
                            redisRepository.lRightPush("company_"+c.getCompanyId()+"_GroupWait_"+c.getGroupId(),c.getUserId().toString());
                        }
                );
                redisRepository.lRightPush("company_"+companyId+"_GroupValue_",k.toString());
            }
        });
    }

    @Override
    public void groupRecover(Long companyId) {
        String s = redisRepository.lRightPop("company_" + companyId + "_GroupValue_");
        redisRepository.lLeftPush("company_" + companyId + "_GroupValue_",s);
    }

    @Override
    public void choseOne2(Long aiUserId, Long companyId, int groupId, Long[] aiUserIds) {
        int len =aiUserIds.length;
        for (int i=0;i<len;i++){
            Long aiUserId1 = aiUserIds[i];
            if (aiUserId.longValue()!=aiUserId1.longValue()) {
                redisRepository.lRemove("company_"+companyId+"_GroupWait_"+groupId,0,aiUserId1+"");
                redisRepository.lRightPush("company_"+companyId+"_GroupWait_"+groupId,aiUserId1+"");
            }
        }
    }

    @Override
    public void setAccepterGroupByCompanyId(Long companyId) {

        List<AiUser> aiUsers = aiUserRepository.getGroupInfo(companyId);
        Map<Integer, List<AiUser>> collect = aiUsers.parallelStream().collect(
                Collectors.groupingBy(AiUser::getGroupId )
        );
        deleteALl(companyId);
        collect.forEach((k,v)->{
                v.stream().sorted(Comparator.comparing(AiUser::getSortIndex)).forEach(c->{
                    if (c.getAccepterStatus().longValue()==0||c.getAcceptTimes()==0){
                        c.setAccepterStatus(1L);
                         if (c.getAcceptTimes()==0) {
                            c.setAcceptTimes(3L);
                        }
                        aiUserRepository.save(c);
                    }
                            redisRepository.lRightPush("company_"+c.getCompanyId()+"_GroupWait_"+c.getGroupId(),c.getUserId().toString());
                        }
                );
                redisRepository.lRightPush("company_"+companyId+"_GroupValue_",k.toString());
        });
    }

    public void deleteALl(Long companyId) {
        while (redisRepository.hasKey("company_" + companyId + "_GroupValue_")) {
            String s= redisRepository.lRightPop("company_" + companyId + "_GroupValue_");

            int groupId=Integer.valueOf(s);
            redisRepository.delete("company_" + companyId + "_GroupWait_" + groupId);
            redisRepository.delete("company_" + companyId + "_GroupWork_" + groupId);
        }
    }

    private boolean CheckRedis(Long companyId){
        Boolean aBoolean = redisRepository.hasKey("company_" + companyId + "_GroupValue_");
        return aBoolean;
    }
    public int getGroup(Long companyId){
        boolean b = haseWorkGroup(companyId);
        if (!b){
            return -1;
        }
        int groupId=-1;
        while (!redisRepository.hasKey("company_" + companyId + "_GroupWait_" + groupId)){
         groupId=Integer.parseInt(redisRepository.lLeftPop("company_"+companyId+"_GroupValue_"));
        redisRepository.lRightPush("company_"+companyId+"_GroupValue_",groupId+"");

        }
        return groupId;
    }
    @Override
    public synchronized List<Long>  getAcceptersBycompanyIdAndGroupId(Long companyId, int count) {
        int groupId;

        if (!redisRepository.hasKey("company_" + companyId + "_GroupValue_")){
            return null;
        }
        groupId=getGroup(companyId);
        List<Long> longs = new ArrayList<>();

        Long len = redisRepository.lLen("company_" + companyId + "_GroupWait_" + groupId);
        count= len>count?count:len.intValue();
        for (int i=0;i<count;i++){
//            Long aiUserId = Long.parseLong(redisRepository.lLeftPopAndLeftPush("company_" + companyId + "_GroupWait_" + groupId,
//                    "company_" + companyId + "_GroupWork_" + groupId));
            Long aiUserId = Long.parseLong(redisRepository.lLeftPop("company_" + companyId + "_GroupWait_" + groupId));
            longs.add(aiUserId);
        }

        return longs;

    }

    @Override
    public void choseOne(Long aiUserId,Long companyId, int groupId) {
        redisRepository.lRemove("company_"+companyId+"_GroupWork_"+groupId,0,aiUserId.toString());
        Long len = redisRepository.lLen("company_"+companyId+"_GroupWork_"+groupId);
        for (int i=0;i<len;i++){
            redisRepository.lLeftPopAndLeftPush("company_"+companyId+"_GroupWork_"+groupId,"company_"+companyId+"_GroupWait_"+groupId);
        }
    }

    @Override
    public void addAccepter(Long aiUserId, Long companyId, int groupId) {
        CheckRedis(companyId);
        if (redisRepository.lhasValue("company_" + companyId + "_GroupValue_", groupId + "") == -1) {
            redisRepository.lRightPush("company_" + companyId + "_GroupValue_", groupId + "");
        }
        redisRepository.lRightPush("company_"+companyId+"_GroupWait_"+groupId,String.valueOf(aiUserId));
    }

    @Override
    public void delAccepter(Long aiUserId, Long companyId, int groupId) {
        CheckRedis(companyId);
        Long lsize = redisRepository.lsize("company_" + companyId + "_GroupWait_" + groupId);
        if(lsize==1){
            redisRepository.lRemove("company_" + companyId + "_GroupValue_",0,groupId+"");
        }
        redisRepository.lRemove("company_"+companyId+"_GroupWait_"+groupId,0,aiUserId+"");
    }


    @Override
    public void delGroup(Long companyId, int groupId) {
        CheckRedis(companyId);
        redisRepository.lRemove("company_"+companyId+"_GroupValue_",0,groupId+"");
        redisRepository.delete("company_"+companyId+"_GroupWait_"+groupId);
    }

    @Override
    public boolean haseWorkGroup(Long companyId) {
        Set<String> keys = redisRepository.keys("company_" + companyId + "_GroupWait_" + "*");
        if (keys.size()>0){
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void backToWait(Long aiUserId,Long companyId, int groupId) {
        redisRepository.lRemove("company_"+companyId+"_GroupWait_"+groupId,0,aiUserId.toString());
        redisRepository.lRightPush("company_"+companyId+"_GroupWait_"+groupId,aiUserId.toString());

    }


}
