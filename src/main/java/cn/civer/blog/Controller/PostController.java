package cn.civer.blog.Controller;

import cn.civer.blog.Model.DTO.PostDTO;
import cn.civer.blog.Model.Entity.MessageConstants;
import cn.civer.blog.Model.Entity.Post;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.PostServ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/post")
public class PostController {
    @Autowired
    private PostServ postServ;

    @PreAuthorize("hasRole('poster')")
    @PostMapping("/add")
    public Result postAdd(@RequestBody PostDTO post){
        postServ.postAdd(post);
        return Result.success(MessageConstants.POST_ADD_SUCCESS);
    }
    @PreAuthorize("hasRole('poster')")
    @DeleteMapping("/delete/{postId}")
    public Result postDeleteByPostId(@PathVariable("postId") BigInteger postId){
        postServ.postDeleteById(postId);
        return Result.success(MessageConstants.POST_DELETE_SUCCESS);
    }
    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/deletebycategory/{categoryId}")
    public Result postDeleteByCategoryId(@PathVariable("categoryId") BigInteger categoryId){
        postServ.postDeleteByCategory(categoryId);
        return Result.success(MessageConstants.POST_DELETE_SUCCESS);
    }
    @PreAuthorize("hasRole('manager')")
    @DeleteMapping("/deletebylabel/{labelId}")
    public Result postDeleteByLabelId(@PathVariable("labelId") BigInteger labelId){
        postServ.postDeleteByLabel(labelId);
        return Result.success(MessageConstants.POST_DELETE_SUCCESS);
    }
    @GetMapping("/selectbypost/{postId}")
    public Result postSelectByPostId(@PathVariable("postId") BigInteger postId){
        postServ.postIncreViews(postId);
        Post post = postServ.postSelectById(postId);
        return Result.success(post);
    }
    @GetMapping("/selectbyuser/{userId}")
    public Result postSelectByUserId(@PathVariable("userId") BigInteger userId){
        List<Post> posts = postServ.postSelectByAuthor(userId);
        return Result.success(posts);
    }
    @GetMapping("/selectbytitle/{title}")
    public Result postSelectByTitle(@PathVariable("title") String title){
        List<Post> posts =  postServ.postSelectByTitle(title);
        return Result.success(posts);
    }
    @GetMapping("/selectbylabel/{labelId}")
    public Result postSelectByLabelId(@PathVariable("labelId") BigInteger labelId){
        List<Post> posts =  postServ.postSelectByLabel(labelId);
        return Result.success(posts);
    }
    @GetMapping("/selectbycategory/{categoryId}")
    public Result postSelectByCategoryId(@PathVariable("categoryId") BigInteger categoryId){
        List<Post> posts = postServ.postSelectByCategory(categoryId);
        return Result.success(posts);
    }
    @GetMapping("/selectbyauthor/{authorId}")
    public Result postSelectByAuthorId(@PathVariable("authorId") BigInteger authorId){
        List<Post> posts = postServ.postSelectByAuthor(authorId);
        return Result.success(posts);
    }
    @GetMapping("/select")
    public Result postSelectAll(){
        List<Post> posts = postServ.postSelectAll();
        return Result.success(posts);
    }
    @PreAuthorize("hasRole('poster')")
    @Caching(evict = {
        @CacheEvict(value = "post", key = "'id:' + #postId"),
        @CacheEvict(value = "postList", allEntries = true)
    })
    @PutMapping("/update")
    public Result postUpdate(@RequestBody PostDTO postDTO,
                             @RequestParam("postId") BigInteger postId){
        postServ.postUpdate(postId,postDTO);
        return Result.success(MessageConstants.POST_UPDATE_SUCCESS);
    }
    @Caching(evict = {
        @CacheEvict(value = "post", key = "'id:' + #postId"),
        @CacheEvict(value = "postList", allEntries = true)
    })
    @PostMapping("/update/like")
    public Result postUpdateLikes(@RequestParam("postId") BigInteger postId){
        postServ.postIncreLikes(postId);
        return Result.success(MessageConstants.POST_LIKE_SUCCESS);
    }
    @Caching(evict = {
        @CacheEvict(value = "post", key = "'id:' + #postId"),
        @CacheEvict(value = "postList", allEntries = true)
    })
    @PostMapping("/update/view")
    public Result postUpdateViews(@RequestParam("postId") BigInteger postId){
        postServ.postIncreViews(postId);
        return Result.success(MessageConstants.POST_VIEW_SUCCESS);
    }
}
