package ecovacs.controller.generic;


import ecovacs.dao.model.ResultModel;
import ecovacs.login.GenericService;
import ecovacs.pojo.AiUser;
import ecovacs.pojo.CustomerFollowUp;
import ecovacs.pojo.CustomerVisit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(
        tags={"登录，注册，统计接口。。。。"}
)
@RestController //只返回json数据，必须要是@Restcontroller
public class GenericController {
    @Autowired
    private GenericService genericService;

    @ApiOperation(value = "登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "mobile",
                    value= "手机号码",
                    required= true,
                    paramType="query",
                    dataType="String")
            ,
            @ApiImplicitParam(
                    name= "pass",
                    value= "密码",
                    required= true,
                    paramType="query",
                    dataType="String")

    } )
    @RequestMapping(value = "/user/go",method = RequestMethod.POST)
    public ResultModel login1 (@RequestParam String mobile, @RequestParam String pass ) {
        if (mobile.length() <= 0 || pass.length() <= 0) {
            return new ResultModel(1002);
        }
        ResultModel result = genericService.login(mobile,pass);
        return result;

    }
    @ApiOperation(value = "统计过去7天客户数量")
    @ApiImplicitParam(
            name= "UserId",
            value= "管理或者置业顾问Id",
            required= true,
            paramType="query",
            dataType="Long")
    @RequestMapping(value = "/getWeek",method = RequestMethod.POST)
    public ResultModel getWeek(@RequestParam Long userId) {
        ResultModel result = genericService.getWeek(userId);
        return result;
    }
    @ApiOperation(value = "不用管，为了显示model信息")
    @ApiImplicitParam(
            name= "CustomerVisit",
            value= "管理或者置业顾问Id",
            required= true,
            paramType="body",
            dataType="CustomerVisit")
    @RequestMapping(value = "/getWeek1",method = RequestMethod.POST)
    public ResultModel getWeek1(@RequestBody CustomerVisit a) {
        return null;
    }
    @ApiOperation(value = "不用管，为了显示model信息")
    @ApiImplicitParam(
            name= "CustomerFollowUp",
            value= "管理或者置业顾问Id",
            required= true,
            paramType="body",
            dataType="CustomerFollowUp")
    @RequestMapping(value = "/getWeek2",method = RequestMethod.POST)
    public ResultModel getWeek2(@RequestBody CustomerFollowUp a) {
        return null;
    }
    @ApiOperation(value = "不用管，为了显示model信息")
    @ApiImplicitParam(
            name= "aiUser",
            value= "管理或者置业顾问Id",
            required= true,
            paramType="body",
            dataType="AiUser")
    @RequestMapping(value = "/getWeek3",method = RequestMethod.POST)
    public ResultModel getWeek3(@RequestBody AiUser aiUser) {
        return null;
    }
}
