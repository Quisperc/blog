package cn.civer.blog.Exception;

import cn.civer.blog.Model.Entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** ✅ 捕获所有未处理的异常 */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error("服务器内部错误，请联系管理员。");
    }

    /** ✅ 数据库异常 */
    @ExceptionHandler(DataAccessException.class)
    public Result handleDataAccessException(DataAccessException e) {
        log.error("数据库访问异常：", e);
        return Result.error("数据库操作失败，请检查数据合法性。");
    }

    /** ✅ 权限异常 */
    @ExceptionHandler(AccessDeniedException.class)
    public Result handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足：{}", e.getMessage());
        return Result.error("无操作权限");
    }

    /** ✅ 参数校验异常（例如 @Valid） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败：{}", msg);
        return Result.error(msg);
    }

    /** ✅ 业务异常 */
    @ExceptionHandler(BizException.class)
    public Result handleBizException(BizException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }
}
