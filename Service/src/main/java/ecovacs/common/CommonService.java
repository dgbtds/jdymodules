package ecovacs.common;




import ecovacs.dao.model.ResultModel;

import java.util.List;
import java.util.Map;

public interface CommonService {

    ResultModel getResult(List<Map> dataArray);

    int getWeekIndex();
}
