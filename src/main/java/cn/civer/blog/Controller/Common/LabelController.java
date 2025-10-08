package cn.civer.blog.Controller.Common;

import cn.civer.blog.Mapper.LabelMapper;
import cn.civer.blog.Model.Entity.MessageConstants;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.LabelServ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/admin/label")
public class LabelController {
    @Autowired
    private LabelServ labelServ;
    @PreAuthorize("hasRole('manager')")
    @PostMapping("/add")
    public Result labelAdd(@RequestParam("title") String title){
        labelServ.labelInsert(title);
        return Result.success(MessageConstants.LABEL_INSERT_SUCCESS);
    }
    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/delete")
    public Result labelDelete(@RequestParam("labelId") BigInteger labelId){
        labelServ.labelDelete(labelId);
        return Result.success(MessageConstants.LABEL_DELETE_SUCCESS);
    }
    @PreAuthorize("hasRole('manager')")
    @PutMapping("/update")
    public Result labelUpdate(@RequestParam("labelId")BigInteger labelId,
                              @RequestParam("title")String title,
                              @RequestParam("status")Integer status){
        labelServ.labelUpdate(labelId,title,status);
        return Result.success(MessageConstants.LABEL_UPDATE_SUCCESS);
    }
    @GetMapping("/selectBylabelId/{labelId}")
    public Result labelSelectById(@PathVariable("labelId") BigInteger labelId){

        return Result.success(labelServ.labelSelectById(labelId));
    }

    @GetMapping("/selectByTitle/{title}")
    public Result labelSelectByTitle(@PathVariable("title") String title){
        return Result.success(labelServ.labelSelectByTitle(title));
    }
    @GetMapping("/select")
    public Result labelSelectByAll(){
        return Result.success(labelServ.labelSelectByAll());
    }
}
