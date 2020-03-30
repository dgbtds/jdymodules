package ecovacs.controller.manager;


import ecovacs.ManagerService.ManagerService;
import ecovacs.dao.model.ResultModel;
import ecovacs.pojo.AiCustomer;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(
        tags={"管理行为接口"}
)
@RestController
@RequestMapping("/manager")
public class ManagerController {
    @Autowired
    private ManagerService managerService;
    @ApiOperation(value = "获得销售顾问在Accepter表登记记录")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "aiUserId",
                    value= "销售顾问Id",
                    required= true,
                    paramType="query",
                    dataType="Long")

    } )
    @RequestMapping(value = "/getAccepterInfo",method = RequestMethod.POST)
    public ResultModel getMyself(@RequestParam Long managerId,@RequestParam Long aiUserId) {
        ResultModel result = managerService.getMyself(managerId,aiUserId);
        return result;
    }
    @ApiOperation(value = "重置redis")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "managerId",
                    value = "管理id",
                    required = true,
                    paramType = "query",
                    dataType = "Long")
        }
           )


    @RequestMapping(value = "/resetRedis",method = RequestMethod.POST)
    public ResultModel resetRedis(@RequestParam Long managerId){
        ResultModel result = managerService.resetRedis(managerId);
        return result;
    }
    @ApiOperation(value = "注册账号")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "mobile",
                    value= "注册手机号",
                    required= true,
                    paramType="query"
                   )
            ,
            @ApiImplicitParam(
                    name= "passwd",
                    value= "密码",
                    required= true,
                    paramType="query"
                   )
            ,
            @ApiImplicitParam(
                    name= "role",
                    value= "权限",
                    required= true,
                    paramType="query"
                   )

    } )
    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public ResultModel register(@RequestParam Long managerId,@RequestParam String mobile,@RequestParam String passwd,@RequestParam String role,@RequestParam String name) {
        ResultModel result = managerService.register(managerId,mobile,passwd,role,name);
        return result;
    }
    @ApiOperation(value = "注册多个销售账号")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "mobile",
                    value= "注册手机号",
                    required= true,
                    paramType="query",
                    allowMultiple=true//数组认定
                   )

    } )
    @RequestMapping(value = "/registerAll",method = RequestMethod.POST)
    public ResultModel registerAll(@RequestParam Long managerId,@RequestParam String[] mobile,@RequestParam String[] name) {
        ResultModel result = managerService.registerALL(managerId,mobile,name);
        return result;
    }
    @ApiOperation(value = "获得销售顾问在User表登记记录")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "aiUserId",
                    value= "销售顾问Id",
                    required= true,
                    paramType="query",
                    dataType="Long")

    } )
    @RequestMapping(value = "/getUserInfo",method = RequestMethod.POST)
    public ResultModel getPriRecord(@RequestParam Long managerId,@RequestParam Long aiUserId) {

        return managerService.getPriRecord(managerId,aiUserId);
    }

    @ApiOperation( value = "转交All：将一个置业顾问的客户全部转交给另一个置业顾问" )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "fromaiUserId",
                    value= "被转交者id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "toaiUserId",
                    value= "转交人",
                    required= true,
                    paramType="query",
                    dataType="Long")
    } )
    @RequestMapping(value ="/transferAll",method = RequestMethod.POST)
    public ResultModel transferAll(@RequestParam Long managerId, @RequestParam  Long fromaiUserId, @RequestParam  Long toaiUserId){

        return  managerService.transferAll(managerId,fromaiUserId,toaiUserId);

    }
    @ApiOperation( value = "转交One：将一个置业顾问的某一个客户转交给另一个置业顾问" )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "fromAiUserId",
                    value= "被转交者id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "toAiUserId",
                    value= "转交人",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "customerId",
                    value= "转交客户Id",
                    required= true,
                    paramType="query",
                    dataType="Long")
    } )
    @RequestMapping(value ="/transferOne",method = RequestMethod.POST)
    public ResultModel transferOne(@RequestParam Long managerId, @RequestParam  Long fromAiUserId, @RequestParam  Long toAiUserId,@RequestParam Long customerId){

        return  managerService.transferOne(managerId,fromAiUserId,toAiUserId,customerId);

    }
    @ApiOperation(
            value = "删除一个组，组内人员回到默认未分组状态"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "groupId",
                    value= "组号",
                    required= true,
                    paramType="query",
                    dataType="int")

    } )
    @RequestMapping(value="/delGroup",method = RequestMethod.POST)
    public ResultModel delGroup(@RequestParam Long managerId,@RequestParam Integer groupId ){
        if(groupId<1){
            return  new ResultModel(1003);
        }

        return managerService.deleteGroup(managerId,groupId);
    }
    @ApiOperation(
            value = "修改一个置业顾问的组号"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "aiUserId",
                    value= "置业顾问id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "groupId",
                    value= "置业顾问组号",
                    required= true,
                    paramType="query",
                    dataType="int")
    } )
    @RequestMapping(value = "/changeGroup",method = RequestMethod.POST)
    public ResultModel changeGroup(@RequestParam Long managerId,@RequestParam Long aiUserId,@RequestParam Integer groupId ){
        if(groupId<0){
            return  new ResultModel(1003);
        }
        return managerService.changeGroup(managerId,aiUserId,groupId);
    }
    @ApiOperation(
            value = "修改一组销售的组号"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "aiUserIds",
                    value= "置业顾问id的数组",
                    required= true,
                    paramType="query",
                    allowMultiple=true,//数组认定
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "groupId",
                    value= "置业顾问组号",
                    required= true,
                    paramType="query",
                    dataType="int")
    } )
    @RequestMapping(value="/changeGroups",method = RequestMethod.POST)
    public ResultModel changeGroups(@RequestParam Long managerId,@RequestParam Long[] aiUserIds,@RequestParam Integer groupId ){
        return managerService.changeGrouplist(managerId,aiUserIds, groupId);
    }
    @ApiOperation(
            value = "修改一个销售的组内接待顺序（不是组内唯一会失败）"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "aiUserId",
                    value= "置业顾问id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "index",
                    value= "置业顾问接待顺序",
                    required= true,
                    paramType="query",
                    dataType="Long")
    } )
    @RequestMapping(value="/setIndex",method = RequestMethod.POST)
    public ResultModel setIndex(@RequestParam Long managerId,@RequestParam Long aiUserId,@RequestParam Long index  ){
        return managerService.setIndex( managerId,  aiUserId,  index);
    }
    @ApiOperation(
            value = "修改一组销售的组内接待顺序（不是组内唯一会失败）"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "aiUserIds",
                    value= "置业顾问id",
                    required= true,
                    allowMultiple=true,//数组认定
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "indexs",
                    value= "置业顾问接待顺序",
                    required= true,
                    allowMultiple=true,//数组认定
                    paramType="query",
                    dataType="Long")
    } )
    @RequestMapping(value="/setIndexList",method = RequestMethod.POST)
    public ResultModel setIndexList(@RequestParam Long managerId,@RequestParam Long[] aiUserIds,@RequestParam Long[] indexs ){

        return managerService.setIndexlist( managerId,  aiUserIds,  indexs);
    }
    @ApiOperation(
            value = "修改一个销售的接待次数"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "aiUserId",
                    value= "置业顾问id",
                    required= true,
                    allowMultiple=false,//数组认定
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "count",
                    value= "置业顾问接待次数",
                    required= true,
                    allowMultiple=false,//数组认定
                    paramType="query",
                    dataType="Long")
    } )
    @RequestMapping(value="/setTimes",method = RequestMethod.POST)
    public ResultModel setAcceptTimes(@RequestParam Long managerId,@RequestParam Long aiUserId,@RequestParam Long count){
        if(count<0){
            return  new ResultModel(1003);
        }
        return managerService.setTimes( managerId,  aiUserId,  count);
    }
    @ApiOperation(
            value = "修改一组销售的接待次数"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "aiUserIds",
                    value= "置业顾问id",
                    required= true,
                    allowMultiple=true,//数组认定
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "counts",
                    value= "置业顾问接待次数",
                    required= true,
                    allowMultiple=true,//数组认定
                    paramType="query",
                    dataType="Long")
    } )
    @RequestMapping(value="/setTimesList",method = RequestMethod.POST)
    public ResultModel setAcceptsTimes(@RequestParam Long managerId,@RequestParam Long[] aiUserIds,@RequestParam Long[] counts ){

        return managerService. setTimesList(managerId,  aiUserIds,  counts);
    }
    @ApiOperation(
            value = "获得一个组所有置业顾问信息"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "groupId",
                    value= "组号",
                    required= true,
                    allowMultiple=false,//数组认定
                    paramType="query",
                    dataType="int")
    } )
    @RequestMapping(value="/getGroupById",method = RequestMethod.POST)
    public ResultModel getGroupbyCompanyAndgroupId(@RequestParam Long managerId,@RequestParam Integer groupId ){
        if(groupId<0){
            return  new ResultModel(1003);
        }
        return managerService. getGroupbyCompanyAndGroupId( managerId,groupId) ;
    }
    @ApiOperation(
            value = "获得所有组信息"
    )
    @ApiImplicitParam(
            name= "managerId",
            value= "管理id",
            required= true,
            paramType="query",
            dataType="Long")
    @RequestMapping(value="/getGroup",method = RequestMethod.POST)
    public ResultModel getGroupbyCompany(@RequestParam Long managerId){

        return managerService. getGroupbyCompany( managerId) ;
    }
    @ApiOperation(
            value = "获得公司所有客户列表"
    )
    @ApiImplicitParam(
            name= "managerId",
            value= "管理id",
            required= true,
            paramType="query",
            dataType="Long")
    @RequestMapping(value="/getCustomerList",method = RequestMethod.POST)
    public ResultModel getCustomerList(@RequestParam Long managerId ){

        return managerService. getCustomerList( managerId) ;
    }
    @ApiOperation(
            value = "设置销售在岗状态"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "aiUserId",
                    value= "置业顾问id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "status",
                    value= "置业顾问在岗与否",
                    required= true,
                    paramType="query",
                    dataType="int")
    } )
    @RequestMapping(value = "/setWorkStatus",method = RequestMethod.POST)
    public ResultModel setWorkStatus(@RequestParam Long managerId,@RequestParam Long aiUserId,@RequestParam Integer status) {
        if(status<0){
            return new ResultModel(1003);
        }
        ResultModel resultModel = managerService.setworkStatus(managerId,aiUserId,status);
        return  resultModel;
    }
    @ApiOperation(
            value = "设置销售评分"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "aiUserId",
                    value= "置业顾问id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "score",
                    value= "置业顾问评分",
                    required= true,
                    paramType="query",
                    dataType="Long")
    } )
    @RequestMapping(value = "/setScore",method = RequestMethod.POST)
    public ResultModel setGrace(@RequestParam Long managerId,@RequestParam Long aiUserId,@RequestParam Long score) {
        if(score<0||score>100){
            return new ResultModel(1003,"评分错误");
        }
        ResultModel resultModel = managerService.setGrace(managerId,aiUserId,score);
        return  resultModel;
    }
    @ApiOperation(
            value = "获得所有转交历史,status:0为主动转交，1为被动转交,其他未转交"
    )
    @ApiImplicitParam(
            name= "managerId",
            value= "管理id",
            required= true,
            paramType="query",
            dataType="Long")
    @RequestMapping(value = "/getPassHistory",method = RequestMethod.POST)
    public ResultModel getPassHistory(@RequestParam Long managerId) {

        ResultModel resultModel = managerService.getPassHistory(managerId);
        return  resultModel;
    }
    @ApiOperation(value = "获得客户详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "customerId",
                    value= "客户Id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "managerId",
                    value= "管理id",
                    required= true,
                    paramType="query",
                    dataType="Long")
    })
    @ApiResponse(code = 200,message = "客户跟进记录",response = AiCustomer.class)
    @RequestMapping(value = "/getCustomerDetail",method = RequestMethod.POST)
    public ResultModel getCustomerDetail( @RequestParam Long customerId,@RequestParam Long managerId) {
        ResultModel result = managerService.getCustomerDetail(customerId,managerId);
        return result;
    }

}
