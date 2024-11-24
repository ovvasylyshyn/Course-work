package org.agency.course_work.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запит на автентифікацію")
public class SignInRequest {

    @Schema(description = "Ім'я користувача", example = "Jon")
    @Size(min = 5, max = 50, message = "Ім'я користувача повинно містити від 5 до 50 символів")
    @NotBlank(message = "Ім'я користувача не може бути порожнім")
    private String username;

    @Schema(description = "Пароль", example = "my_1secret1_password")
    @Size(min = 8, max = 255, message = "Довжина пароля повинна бути від 8 до 255 символів")
    @NotBlank(message = "Пароль не може бути порожнім")
    private String password;
}
