package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.DataTransfer.AccountData.AccountDtos.*;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.ApiException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/** Sign-up and sign-in holes found in the code review. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:accountsecurity;DB_CLOSE_DELAY=-1")
class AccountSecurityTest {

    @Autowired AccountService accounts;
    @Autowired BotPlayer bot;
    @MockitoBean EmailService email;

    private String lastCodeSentTo(String to) {
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(email, atLeastOnce()).sendVerificationCode(eq(to), anyString(), code.capture());
        return code.getValue();
    }

    @Test
    void verifyingNeedsThePasswordSoASecondSignUpCanNotTakeTheAccountOver() {
        String victim = "victim@example.com";
        accounts.register(new RegisterRequest(victim, "Victim", "victim-pass-1", "United States"));
        String code = lastCodeSentTo(victim);

        // the right code with the wrong password is refused
        assertThatThrownBy(() -> accounts.verify(new VerifyRequest(victim, code, "attacker-pass")))
                .isInstanceOf(ApiException.class).hasMessageContaining("code or password");
        LoginResponse ok = accounts.verify(new VerifyRequest(victim, code, "victim-pass-1"));
        assertThat(ok.token()).isNotBlank();
    }

    @Test
    void nobodyCanRegisterOverOrSignInAsThePracticeBot() {
        String botEmail = bot.get().getEmail();
        assertThatThrownBy(() -> accounts.register(new RegisterRequest(botEmail, "Hijacker", "password123", "United States")))
                .isInstanceOf(ApiException.class).hasMessageContaining("already exists");
        assertThat(bot.get().getDisplayName()).startsWith("Training Bot");
        assertThatThrownBy(() -> accounts.login(new LoginRequest(botEmail, "anything1")))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> accounts.verify(new VerifyRequest(botEmail, "000000", "anything1")))
                .isInstanceOf(ApiException.class).hasMessageContaining("Invalid or expired");
    }
}
