package net.security.infosec.validations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.dto.PasswordDTO;
import net.security.infosec.models.Implementer;
import net.security.infosec.services.ImplementerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordValidation implements Validator {
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(PasswordDTO.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordDTO passwordDTO = (PasswordDTO) target;
        checkPassword(passwordDTO,errors);
    }

    public void checkOldPassword(PasswordDTO passwordDTO, Errors errors, Implementer implementer){
        if(!passwordEncoder.matches(passwordDTO.getOldPassword(), implementer.getPassword())){
            errors.rejectValue("oldPassword", "", "Неверный пароль");
        }
    }

    private void checkPassword(PasswordDTO passwordDTO, Errors errors){
        if(passwordDTO.getOldPassword().equals(passwordDTO.getNewPassword())){
            errors.rejectValue("newPassword", "", "Старый и новый пароль не должны совпадать");
        }
        if(!passwordDTO.getNewPassword().equals(passwordDTO.getNewConfirm())){
            errors.rejectValue("newConfirm", "", "Пароль не совпадает");
        }
    }
}
