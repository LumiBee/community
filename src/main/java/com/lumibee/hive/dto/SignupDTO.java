package com.lumibee.hive.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupDTO {

    @NotEmpty(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3到20个字符之间")
    private String username;

    @NotEmpty(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotEmpty(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度必须在6到30个字符之间")
    private String password;

    @NotEmpty(message = "确认密码不能为空")
    private String confirmPassword;

}