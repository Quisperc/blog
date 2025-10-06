package cn.civer.blog.Controller.Common;

import cn.civer.blog.Model.Entity.MessageConstants;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.CategoryServ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/admin/category")
public class CategoryController {
    @Autowired
    private CategoryServ categoryServ;
    @PreAuthorize("hasRole('manager')")
    @PostMapping("/add")
    public Result categoryAdd(String title, String summary){
        categoryServ.categoryInsert(title,summary);
        return Result.success(MessageConstants.CATEGORY_INSERT_SUCCESS);
    }
    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/delete")
    public Result categoryDelete(BigInteger categoryId){
        categoryServ.categoryDelete(categoryId);
        return Result.success(MessageConstants.CATEGORY_DELETE_SUCCESS);
    }
    @PreAuthorize("hasRole('manager')")
    @PutMapping("/update")
    public Result categoryUpdate(BigInteger categoryId,String title, String summary,Integer status){
        categoryServ.categoryUpdate(categoryId,title,summary,status);
        return Result.success(MessageConstants.CATEGORY_UPDATE_SUCCESS);
    }
    @GetMapping("/selectBycategoryId/{categoryId}")
    public Result categorySelectById(@PathVariable BigInteger categoryId){
        
        return Result.success(categoryServ.categorySelectById(categoryId));
    }

    @GetMapping("/selectByTitle/{title}")
    public Result categorySelectByTitle(@PathVariable String title){
        return Result.success(categoryServ.categorySelectByTitle(title));
    }
    @GetMapping("/select")
    public Result categorySelectByAll(){
        return Result.success(categoryServ.categorySelectByAll());
    }
}
