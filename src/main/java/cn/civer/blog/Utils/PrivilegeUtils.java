package cn.civer.blog.Utils;

import cn.civer.blog.Model.Enum.Role;
import cn.civer.blog.Model.Entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PrivilegeUtils {
    public static List<String> getPri(User user){
        List arrayList = new ArrayList();
        if(user.getRole() == Role.manager){
            // 添加权限集合
            arrayList.add("ROLE_"+ Role.manager);
            arrayList.add("ROLE_"+ Role.poster);
            arrayList.add("ROLE_"+ Role.viewer);
        } else if (user.getRole() == Role.poster) {
            arrayList.add("ROLE_"+ Role.poster);
            arrayList.add("ROLE_"+ Role.viewer);
        }else {
            arrayList.add("ROLE_"+ Role.viewer);
        }
        return arrayList;
    }
}
