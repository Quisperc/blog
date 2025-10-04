package cn.civer.blog.Controller.Common;

import cn.civer.blog.Mapper.LabelMapper;
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
    public Result labelAdd(String title, String summary){
        return  labelServ.labelInsert(title,summary);
    }
    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/delete")
    public Result labelDelete(BigInteger labelId){
        return labelServ.labelDelete(labelId);
    }
    @PreAuthorize("hasRole('manager')")
    @PutMapping("/update")
    public Result labelUpdate(BigInteger labelId,String title, String summary,Integer status){
        return labelServ.labelUpdate(labelId,title,summary,status);
    }
    @GetMapping("/selectBylabelId/{labelId}")
    public Result labelSelectById(@PathVariable BigInteger labelId){
        return labelServ.labelSelectById(labelId);
    }

    @GetMapping("/selectByTitle/{title}")
    public Result labelSelectByTitle(@PathVariable String title){
        return labelServ.labelSelectByTitle(title);
    }
    @GetMapping("/select")
    public Result labelSelectByAll(){
        return labelServ.labelSelectByAll();
    }
}
