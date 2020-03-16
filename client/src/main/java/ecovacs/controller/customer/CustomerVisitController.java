package ecovacs.controller.customer;


import ecovacs.customer.CustomerService;
import ecovacs.dao.model.ResultModel;
import ecovacs.pojo.AiCustomer;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(
        tags={"客户访问接口"}
)
@RestController
@RequestMapping("/customer")
public class CustomerVisitController {
    @Autowired
    private CustomerService customerService;

    @ApiOperation(value = "客户到来")
    @ApiImplicitParam(name = "robotId",
            value = "robotId（String）值，为其sn号",
            required = true,
            paramType = "query",
            dataType = "String"
    )
    @ApiResponse(code = 200,message = "返回销售列表，长度--》最大为3最小1",response = AiCustomer.class)
    @RequestMapping(value = "/first",method = RequestMethod.POST)
    public ResultModel firstVisit(@RequestParam String robotId){
        return  customerService.firstVisit(robotId);
    }

    @ApiOperation(value = "新版-->客户选择一个销售")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "aiUserId",
                    value= "选择的销售Id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "customerLogo",
                    value= "客户图像地址",
                    required= true,
                    paramType="query",
                    dataType="String")
            ,
            @ApiImplicitParam(
                    name= "aiUserIds",
                    value= "可选销售列表",
                    required= true,
                    paramType="query",
                    allowMultiple=true,//数组认定
                    dataType="Long")
    } )
    @RequestMapping(value = "/choseOne2",method = RequestMethod.POST)
    public ResultModel choseone2(@RequestParam Long aiUserId ,@RequestParam String customerLogo ,@RequestParam Long[] aiUserIds){
        return  customerService.choseone2(aiUserId,customerLogo,aiUserIds);

    }
    @ApiOperation(value = "旧版-->客户选择一个销售")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name= "aiUserId",
                    value= "选择的销售Id",
                    required= true,
                    paramType="query",
                    dataType="Long")
            ,
            @ApiImplicitParam(
                    name= "customerLogo",
                    value= "客户图像地址",
                    required= true,
                    paramType="query",
                    dataType="String")

    } )
    @RequestMapping(value = "/choseOne",method = RequestMethod.POST)
    public ResultModel choseone1(@RequestParam Long aiUserId ,@RequestParam String customerLogo ){
        return  customerService.choseone(aiUserId,customerLogo);

    }

}
