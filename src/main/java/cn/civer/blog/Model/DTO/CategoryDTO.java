package cn.civer.blog.Model.DTO;

import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryDTO implements Serializable {
    private String title;
    private String summary;
}
