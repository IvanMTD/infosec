package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.repositories.AppSettingsRepository;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Создаёт LdapTemplate динамически из настроек БД.
 * Не падает при старте если LDAP недоступен.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicLdapTemplate {

    private final AppSettingsService appSettingsService;
    private final AppSettingsRepository appSettingsRepository;
    private final LdapPasswordCrypto crypto;

    /**
     * Создаёт LdapTemplate на основе настроек из БД.
     * Возвращает null если настройки неполные.
     */
    public LdapTemplate createTemplate() {
        Map<String, String> config = appSettingsService.getLdapConfig().block();
        if (config == null) return null;

        String enabled = config.getOrDefault("ldap.sync.enabled", "false");
        if (!"true".equalsIgnoreCase(enabled)) return null;

        String url = config.getOrDefault("ldap.url", "");
        String base = config.getOrDefault("ldap.base", "");
        String bindDn = config.getOrDefault("ldap.bind.dn", "");
        // Пароль читаем напрямую из БД (getLdapConfig его маскирует)
        String encryptedPass = appSettingsRepository.findByKey("ldap.bind.pass")
            .map(settings -> settings.getValue()).defaultIfEmpty("").block();
        String bindPass = crypto.decrypt(encryptedPass != null ? encryptedPass : "");

        if (url.isBlank() || base.isBlank() || bindDn.isBlank() || bindPass.isBlank()) {
            log.warn("LDAP настройки неполные: url={}, base={}, bindDn={}, pass=***", url, base, bindDn);
            return null;
        }

        log.info("Создаю LdapContextSource: url={}, base={}, bindDn={}", url, base, bindDn);
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(url);
        contextSource.setBase(base);
        contextSource.setUserDn(bindDn);
        contextSource.setPassword(bindPass);
        try {
            contextSource.afterPropertiesSet();
            log.info("LdapContextSource создан успешно");
            return new LdapTemplate(contextSource);
        } catch (Exception e) {
            log.error("Не удалось создать LdapTemplate: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Проверяет соединение с LDAP, возвращает null если OK, или сообщение об ошибке.
     */
    public String testConnection() {
        LdapTemplate template = createTemplate();
        if (template == null) return "Не удалось создать подключение — проверьте настройки";
        try {
            // Реальный поиск — проверяет что base DN существует
            template.search("", "(objectClass=*)", (org.springframework.ldap.core.AttributesMapper<Object>) attrs -> null);
            return null; // OK
        } catch (Exception e) {
            log.warn("LDAP test failed: {}", e.getMessage());
            return e.getMessage();
        }
    }
}
