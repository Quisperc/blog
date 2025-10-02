package cn.civer.blog.Controller;

import cn.civer.blog.Model.DTO.PostDTO;
import cn.civer.blog.Model.Entity.Post;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.PostServ;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/post")
public class PostController {
    @Autowired
    private PostServ postServ;

    @PostMapping("/add")
    public Result postAdd(@RequestBody PostDTO post){
        return postServ.postAdd(post);
    }
    @DeleteMapping("/delete/{postId}")
    public Result postDeleteByPostId(@PathVariable BigInteger postId){
        return postServ.postDeleteById(postId);
    }
    @DeleteMapping("/delete/{categoryId}")
    public Result postDeleteByCategoryId(@PathVariable BigInteger categoryId){
        return postServ.postDeleteByCategory(categoryId);
    }
    @DeleteMapping("/delete/{labelId}")
    public Result postDeleteByLabelId(@PathVariable BigInteger labelId){
        return postServ.postDeleteByLabel(labelId);
    }
    @GetMapping("/select/{postId}")
    public Result postSelectByPostId(@PathVariable BigInteger postId){
        return postServ.postSelectById(postId);
    }
    @GetMapping("/select/{categoryId}")
    public Result postSelectByTitle(@PathVariable String title){
        return postServ.postSelectByTitle(title);
    }
    @GetMapping("/select/{labelId}")
    public Result postSelectByLabelId(@PathVariable BigInteger labelId){
        return postServ.postSelectByLabel(labelId);
    }
    @GetMapping("/select/{categoryId}")
    public Result postSelectByCategoryId(@PathVariable BigInteger categoryId){
        return postServ.postSelectByCategory(categoryId);
    }
    @GetMapping("/select")
    public Result postSelectAll(){
        return postServ.postSelectAll();
    }
}
