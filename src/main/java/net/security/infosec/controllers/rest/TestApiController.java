package net.security.infosec.controllers.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.services.LdapSyncService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestApiController {

    private final LdapTemplate ldapTemplate;
    private final LdapSyncService ldapSyncService;

    @Value("${spring.ldap.base}")
    private String baseDn;

    @GetMapping("/ldap/users")
    public List<Map<String, Object>> ldapUsers(@RequestParam(defaultValue = "") String search) {
        String filter;
        if (search.isBlank()) {
            filter = "(&(objectCategory=person)(mail=*)"
                   + "(|(&(objectClass=user)(givenName=*)(sn=*)"
                   +   "(!(userAccountControl:1.2.840.113556.1.4.803:=2)))"
                   +   "(objectClass=contact))"
                   + ")";
        } else {
            filter = "(&(objectCategory=person)(mail=*)"
                   + "(|(&(objectClass=user)(givenName=*)(sn=*)"
                   +   "(!(userAccountControl:1.2.840.113556.1.4.803:=2)))"
                   +   "(objectClass=contact))"
                   + "(cn=*" + search + "*))";
        }
        try {
            return ldapTemplate.search(baseDn, filter, new LdapUserMapper());
        } catch (Exception e) {
            log.error("LDAP error", e);
            return List.of(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/ldap/ous")
    public List<Map<String, Object>> ldapOUs() {
        try {
            return ldapTemplate.search(baseDn, "(objectClass=organizationalUnit)", new LdapOUMapper());
        } catch (Exception e) {
            log.error("LDAP error", e);
            return List.of(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/sync-preview")
    public List<Map<String, Object>> syncPreview(@RequestParam(defaultValue = "") String search) {
        return ldapSyncService.preview(search);
    }

    @GetMapping("/sync-dry-run")
    public Map<String, Object> syncDryRun() {
        return ldapSyncService.dryRun();
    }

    private String safeGetAttr(Attributes attrs, String name) {
        try {
            Attribute attr = attrs.get(name);
            return attr != null ? String.valueOf(attr.get(0)) : "";
        } catch (Exception e) {
            return "";
        }
    }

    private class LdapUserMapper implements org.springframework.ldap.core.AttributesMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapFromAttributes(Attributes attrs) throws NamingException {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("dn",              safeGetAttr(attrs, "distinguishedName"));
            map.put("cn",              safeGetAttr(attrs, "cn"));
            map.put("sAMAccountName",  safeGetAttr(attrs, "sAMAccountName"));
            map.put("displayName",     safeGetAttr(attrs, "displayName"));
            map.put("givenName",       safeGetAttr(attrs, "givenName"));
            map.put("sn",              safeGetAttr(attrs, "sn"));
            map.put("title",           safeGetAttr(attrs, "title"));
            map.put("department",      safeGetAttr(attrs, "department"));
            map.put("mail",            safeGetAttr(attrs, "mail"));
            map.put("telephoneNumber", safeGetAttr(attrs, "telephoneNumber"));
            map.put("mobile",          safeGetAttr(attrs, "mobile"));
            map.put("otherTelephone",  safeGetAttr(attrs, "otherTelephone"));
            return map;
        }
    }

    private class LdapOUMapper implements org.springframework.ldap.core.AttributesMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapFromAttributes(Attributes attrs) throws NamingException {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("dn",          safeGetAttr(attrs, "distinguishedName"));
            map.put("ou",          safeGetAttr(attrs, "ou"));
            map.put("name",        safeGetAttr(attrs, "name"));
            map.put("description", safeGetAttr(attrs, "description"));
            return map;
        }
    }
}
