package cn.civer.blog.Controller.Common;

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
        return  categoryServ.categoryInsert(title,summary);
    }
    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/delete")
    public Result categoryDelete(BigInteger categoryId){
        return categoryServ.categoryDelete(categoryId);
    }
    @PreAuthorize("hasRole('manager')")
    @PutMapping("/update")
    public Result categoryUpdate(BigInteger categoryId,String title, String summary,Integer status){
        return categoryServ.categoryUpdate(categoryId,title,summary,status);
    }
    @GetMapping("/selectByCategoryId/{categoryId}")
    public Result categorySelectById(@PathVariable BigInteger categoryId){
        return categoryServ.categorySelectById(categoryId);
    }

    @GetMapping("/selectByTitle/{title}")
    public Result categorySelectByTitle(@PathVariable String title){
        return categoryServ.categorySelectByTitle(title);
    }
    @GetMapping("/select")
    public Result categorySelectByAll(){
        return categoryServ.categorySelectByAll();
    }
}
