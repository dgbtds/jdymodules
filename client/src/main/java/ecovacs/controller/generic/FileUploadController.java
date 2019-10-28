package ecovacs.controller.generic;


import ecovacs.Util.NonUtil;
import ecovacs.dao.model.ResultModel;
import ecovacs.dao.pojoRepository.AiUserRepository;
import ecovacs.dao.pojoRepository.RobotRecordRepository;
import ecovacs.pojo.AiUser;
import ecovacs.pojo.RobotRecord;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@Api( tags={"上传文件"})
@RequestMapping("/company")
public class FileUploadController {
    @Autowired
    AiUserRepository aiUserRepository;
    @Autowired
    RobotRecordRepository robotRecordRepository;
    @Value("${base.path}")
    private String basepath;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadController.class);
    @ApiOperation(value = "置业顾问上传文件,必须带上传文件的后缀名，上传xxx.jpg则应该命名yyyy.jpg")
    @ApiImplicitParams({

            @ApiImplicitParam(
                    name = "aiUserId",
                    value ="置业顾问Id",
                    required = true,
                    paramType = "query",
                    dataType = "Long"
            )
    }
    )
    //上传文件必须指定参数名为file
    @RequestMapping(value ="/uploadUserLogo", method = RequestMethod.POST,headers = "content-type=multipart/form-data")
    public ResultModel uploadLogoUser(@RequestParam Long aiUserId, @RequestParam(value = "file") MultipartFile accepterPhoto ){

        AiUser byUserId = aiUserRepository.findByUserId(aiUserId);
        Long companyId=byUserId.getCompanyId();
        // 获取文件名
        String fileName = accepterPhoto.getOriginalFilename();
        // 获取文件的后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));

        String prefixName = basepath+ File.separator+"img"+File.separator;
        //String path =prefixName;
        String filename = prefixName +"company_"+ companyId+ File.separator+"_accepter_"+aiUserId+ File.separator +
                new SimpleDateFormat("yyyy-mm-dd").format(new Date())+suffixName;
        ResultModel resultModel = upload(accepterPhoto, filename);
        if(resultModel.getStatus()==0){
            byUserId.setUpLogo(filename);
            aiUserRepository.save(byUserId);
        }
        return  resultModel;
    }
    @ApiOperation(value = "客户上传文件,文件名file")
    @ApiImplicitParams({

            @ApiImplicitParam(
                    name = "robotId",
                    value ="机器人Id",
                    required = true,
                    paramType = "query",
                    dataType = "String"
            ),
            @ApiImplicitParam(
                    name = "customerLogo",
                    value ="客户头像名，必须带上传文件的后缀名，上传xxx.jpg则应该命名yyyy.jpg",
                    required = true,
                    paramType = "query",
                    dataType = "String"
            )
    }
    )
    //上传文件必须指定参数名为file
    @RequestMapping(value ="/uploadCustomerLogo", method = RequestMethod.POST,headers = "content-type=multipart/form-data")
    public ResultModel uploadLogoRobot(@RequestParam String robotId,@RequestParam String customerLogo,@RequestParam(value = "file") MultipartFile accepterPhoto ){
        RobotRecord robotRecord = robotRecordRepository.findBySn(robotId);
        if(NonUtil.isNon(robotRecord)) {
            return new ResultModel(1,"所传机器人sn有误");
        }
        Long companyId=robotRecord.getCompanyId();

        String prefixName = basepath+File.separator+"img"+File.separator;

        String path = prefixName +"company_"+ companyId+ File.separator+"Customer"+ File.separator +customerLogo;
        ResultModel resultModel = upload(accepterPhoto, path);

        return  resultModel;
    }
    public ResultModel upload(MultipartFile file,String filePath){
        if (file.isEmpty()) {
            return new ResultModel(1003,"上传失败，请选择文件");
        }
        String path = filePath.substring(0,filePath.lastIndexOf(File.separator));
        File dir=new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File dest = new File(filePath);
        try {
            file.transferTo(dest);
            LOGGER.info(dest+"上传成功");
            return  new ResultModel(0,filePath );
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
        }
        return new ResultModel(1003,"上传失败");
    }
    @ApiOperation(value = "下载客户头像")
    @ApiImplicitParams({

            @ApiImplicitParam(
                    name = "aiUserId",
                    value ="顾问Id",
                    required = true,
                    paramType = "query",
                    dataType = ""
            ),
            @ApiImplicitParam(
                    name = "customerLogo",
                    value ="客户头像名,带后缀",
                    required = true,
                    paramType = "query",
                    dataType = "String"
            )
    }
    )
    @PostMapping("/downloadCustomer")
    public ResultModel downloadCustomer(@RequestParam Long aiUserId, @RequestParam String customerLogo, HttpServletResponse response) throws IOException {
        AiUser aiUser = aiUserRepository.findByUserId(aiUserId);
        if (aiUser==null){
            return new ResultModel(1003,"aiUserId错误");
        }
        String prefixName = basepath+ File.separator+"img"+File.separator;
        String filename = prefixName +"company_"+ aiUser.getCompanyId()+ File.separator+"Customer"+ File.separator +customerLogo;
        File file=new File(filename);
        if(!file.exists()){
            return new ResultModel(1003,"file路径错误,请确认客户图片上传");
        }
        FileInputStream fileInputStream=null;
        ServletOutputStream outputStream=null;
        BufferedInputStream bufferedInputStream=null;
        response.setContentType( "application/octet-stream;charset=UTF-8");
        //不是规定头，设置下载文件文件名
        response.setHeader("Content-Disposition", "attachment;fileName="+ java.net.URLEncoder.encode(customerLogo,"UTF-8"));
        try {
            fileInputStream= new FileInputStream(file);
            outputStream= response.getOutputStream();
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            byte[] buffer=new byte[1024*1024];
            int read=0;
            while (read!=-1){
                read = bufferedInputStream.read(buffer, 0, buffer.length );
                if(read==-1){
                    break;
                }
                outputStream.write(buffer,0,read);
            }

        }catch (Exception e){
            return new ResultModel(1003,"file下载失败");
        }
        finally {
            fileInputStream.close();
            outputStream.close();
            bufferedInputStream.close();
        }
        return new ResultModel(0);
    }

    @ApiOperation(value = "下载置业顾问头像")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "aiUserId",
                    value ="顾问Id",
                    required = true,
                    paramType = "query",
                    dataType = ""
            )
    }
    )
    @PostMapping("/downloadAccepter")
    public ResultModel downloadAccepter(@RequestParam Long aiUserId,  HttpServletResponse response) throws IOException {
        AiUser aiUser = aiUserRepository.findByUserId(aiUserId);
        if (aiUser==null){
            return new ResultModel(1003,"aiUserId错误");
        }
        String filename = aiUser.getUpLogo();
        if (NonUtil.isNon(filename)) {
            return new ResultModel(5,"置业顾问未上传头像");
        }
        File file=new File(filename);
        if(!file.exists()){
            return new ResultModel(1003,"置业顾问file路径错误,");
        }
        FileInputStream fileInputStream=null;
        ServletOutputStream outputStream=null;
        BufferedInputStream bufferedInputStream=null;
        response.setContentType( "application/octet-stream;charset=UTF-8");
        //不是规定头，设置下载文件文件名
        response.setHeader("Content-Disposition", "attachment;fileName="+ java.net.URLEncoder.encode(filename,"UTF-8"));
        try {
            fileInputStream= new FileInputStream(file);
            outputStream= response.getOutputStream();
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            byte[] buffer=new byte[1024*1024];
            int read=0;
            while (read!=-1){
                read = bufferedInputStream.read(buffer, 0, buffer.length );
                if(read==-1){
                    break;
                }
                outputStream.write(buffer,0,read);
            }

        }catch (Exception e){
            return new ResultModel(1003,"file下载失败");
        }
        finally {
            fileInputStream.close();
            outputStream.close();
            bufferedInputStream.close();
        }
        return new ResultModel(0);
    }
}
