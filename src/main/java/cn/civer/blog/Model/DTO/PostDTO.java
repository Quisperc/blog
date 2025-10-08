package cn.civer.blog.Model.DTO;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PostDTO implements Serializable {
    private String title;
    private String summary;
    private String content;
    private List<CategoryDTO> categories;
    private List<LabelDTO> labels;
    private Integer status;
}
