package cn.civer.blog.Controller.Common;

import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.CategoryServ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/admin/category")
public class CategoryController {
    @Autowired
    private CategoryServ categoryServ;
    @PostMapping("/add")
    public Result categoryAdd(String title, String summary){
        return  categoryServ.categoryInsert(title,summary);
    }
    @DeleteMapping("/delete")
    public Result categoryDelete(BigInteger categoryId){
        return categoryServ.categoryDelete(categoryId);
    }
    @PutMapping("/update")
    public Result categoryUpdate(BigInteger categoryId,String title, String summary,Integer status){
        return categoryServ.categoryUpdate(categoryId,title,summary,status);
    }
    @GetMapping("/select/{categoryId}")
    public Result categorySelectById(@PathVariable BigInteger categoryId){
        return categoryServ.categorySelectById(categoryId);
    }

    @GetMapping("/select/{title}")
    public Result categorySelectByTitle(@PathVariable String title){
        return categoryServ.categorySelectByTitle(title);
    }
}
