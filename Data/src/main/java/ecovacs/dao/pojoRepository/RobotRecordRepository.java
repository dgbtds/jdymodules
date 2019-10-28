package ecovacs.dao.pojoRepository;


import ecovacs.pojo.RobotRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RobotRecordRepository extends JpaRepository<RobotRecord,Long> {

    RobotRecord findBySn(String sn);

}
