package cn.civer.blog.Controller;

import cn.civer.blog.Model.DTO.PostDTO;
import cn.civer.blog.Model.Entity.Post;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.PostServ;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/post")
public class PostController {
    @Autowired
    private PostServ postServ;

    @PreAuthorize("hasRole('manager')")
    @PostMapping("/add")
    public Result postAdd(@RequestBody PostDTO post){
        return postServ.postAdd(post);
    }
    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/delete/{postId}")
    public Result postDeleteByPostId(@PathVariable BigInteger postId){
        return postServ.postDeleteById(postId);
    }
    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/deletebycategory/{categoryId}")
    public Result postDeleteByCategoryId(@PathVariable BigInteger categoryId){
        return postServ.postDeleteByCategory(categoryId);
    }
    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/deletebylabel/{labelId}")
    public Result postDeleteByLabelId(@PathVariable BigInteger labelId){
        return postServ.postDeleteByLabel(labelId);
    }
    @GetMapping("/selectbypost/{postId}")
    public Result postSelectByPostId(@PathVariable BigInteger postId){
        return postServ.postSelectById(postId);
    }
    @GetMapping("/selectbytitle/{title}")
    public Result postSelectByTitle(@PathVariable String title){
        return postServ.postSelectByTitle(title);
    }
    @GetMapping("/selectbylabel/{labelId}")
    public Result postSelectByLabelId(@PathVariable BigInteger labelId){
        return postServ.postSelectByLabel(labelId);
    }
    @GetMapping("/selectbycategory/{categoryId}")
    public Result postSelectByCategoryId(@PathVariable BigInteger categoryId){
        return postServ.postSelectByCategory(categoryId);
    }
    @GetMapping("/select")
    public Result postSelectAll(){
        return postServ.postSelectAll();
    }
    @PreAuthorize("hasRole('manager')")
    @PutMapping("/update")
    public Result postUpdate(@RequestBody PostDTO postDTO, BigInteger postId){
        return postServ.postUpdate(postId,postDTO);
    }
}
