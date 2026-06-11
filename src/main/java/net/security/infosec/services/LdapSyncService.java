package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.models.entity.Department;
import net.security.infosec.models.entity.Division;
import net.security.infosec.models.entity.Employee;
import net.security.infosec.repositories.DepartmentRepository;
import net.security.infosec.repositories.DivisionRepository;
import net.security.infosec.repositories.EmployeeRepository;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LdapSyncService {

    private final LdapTemplate ldapTemplate;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DivisionRepository divisionRepository;
    private final DatabaseClient databaseClient;

    @org.springframework.beans.factory.annotation.Value("${spring.ldap.base}")
    private String baseDn;

    /**
     * Автоматическая синхронизация раз в час.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void scheduledSync() {
        log.info("Запуск плановой синхронизации с AD...");
        try {
            Map<String, Object> result = syncAll();
            log.info("Плановая синхронизация завершена: {}", result);
        } catch (Exception e) {
            log.error("Ошибка плановой синхронизации", e);
        }
    }

    /**
     * Предпросмотр синхронизации: сравнивает данные из AD и БД.
     * Ничего не записывает в БД.
     */
    public List<Map<String, Object>> preview(String search) {
        // 1. Поиск в AD
        String filter;
        if (search == null || search.isBlank()) {
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

        List<Map<String, Object>> adUsers;
        try {
            adUsers = ldapTemplate.search(baseDn, filter, new AdUserMapper());
        } catch (Exception e) {
            log.error("LDAP search error", e);
            return List.of(Map.of("error", e.getMessage()));
        }

        // 2. Для каждого — ищем в БД
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> adUser : adUsers) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("ad", adUser);

            String mail = (String) adUser.get("mail");
            if (mail != null && !mail.isBlank()) {
                row.put("db", findInDb(mail));
            } else {
                row.put("db", Map.of("status", "NO_EMAIL"));
            }
            result.add(row);
        }
        return result;
    }

    /**
     * Ищет сотрудника в БД по email и собирает department/division.
     */
    private Map<String, Object> findInDb(String mail) {
        return employeeRepository.findByEmailIgnoreCase(mail)
            .next()
            .map(emp -> {
                Map<String, Object> db = new LinkedHashMap<>();
                db.put("id", emp.getId());
                db.put("lastname", emp.getLastname());
                db.put("name", emp.getName());
                db.put("middleName", emp.getMiddleName());
                db.put("position", emp.getPosition());
                db.put("email", emp.getEmail());
                db.put("phone", emp.getPhone());
                db.put("personalPhone", emp.getPersonalPhone());
                db.put("cabinet", emp.getCabinet());
                db.put("address", emp.getAddress());
                db.put("departmentId", emp.getDepartmentId());
                db.put("divisionId", emp.getDivisionId());
                // Загружаем названия синхронно (блокирующий вызов — допустимо для превью)
                if (emp.getDepartmentId() > 0) {
                    departmentRepository.findById(emp.getDepartmentId())
                        .map(Department::getTitle)
                        .subscribe(title -> db.put("departmentTitle", title));
                }
                if (emp.getDivisionId() > 0) {
                    divisionRepository.findById(emp.getDivisionId())
                        .map(Division::getTitle)
                        .subscribe(title -> db.put("divisionTitle", title));
                }
                return db;
            })
            .defaultIfEmpty(Map.of("status", "NOT_FOUND"))
            .block(); // блокируем для превью
    }

    private String safeGetAttr(Attributes attrs, String name) {
        try {
            Attribute attr = attrs.get(name);
            return attr != null ? String.valueOf(attr.get(0)) : "";
        } catch (Exception e) {
            return "";
        }
    }

    private class AdUserMapper implements AttributesMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapFromAttributes(Attributes attrs) throws NamingException {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("dn",              safeGetAttr(attrs, "distinguishedName"));
            map.put("cn",              safeGetAttr(attrs, "cn"));
            map.put("sAMAccountName",  safeGetAttr(attrs, "sAMAccountName"));
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

    // ================================================================
    // Полная сверка (dry run) — ничего не записывает
    // ================================================================

    public Map<String, Object> dryRun() {
        // 1. Все активные из AD
        List<Map<String, Object>> adUsers;
        try {
            adUsers = ldapTemplate.search(baseDn,
                "(&(objectCategory=person)(mail=*)(|(&(objectClass=user)(givenName=*)(sn=*)(!(userAccountControl:1.2.840.113556.1.4.803:=2)))(objectClass=contact)))",
                new AdUserMapper());
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }

        // 2. Все сотрудники из БД
        List<Employee> dbEmployees = employeeRepository.findAll().collectList().block();
        if (dbEmployees == null) dbEmployees = List.of();

        // 3. Индексы: AD по email, БД по email
        Map<String, Map<String, Object>> adByEmail = new LinkedHashMap<>();
        for (Map<String, Object> ad : adUsers) {
            String mail = ((String) ad.get("mail")).toLowerCase();
            adByEmail.put(mail, ad);
        }

        Map<String, Employee> dbByEmail = new LinkedHashMap<>();
        for (Employee emp : dbEmployees) {
            if (emp.getEmail() != null) {
                dbByEmail.put(emp.getEmail().toLowerCase(), emp);
            }
        }

        Set<String> processedEmails = new HashSet<>();

        // 4. Группируем AD по департаментам → дивижинам
        // Для каждого AD пользователя парсим DN → deptName, divName
        List<Map<String, Object>> deptList = new ArrayList<>();
        Map<String, Map<String, Object>> deptMap = new LinkedHashMap<>();

        for (Map<String, Object> ad : adUsers) {
            String dn = (String) ad.get("dn");
            String mail = ((String) ad.get("mail")).toLowerCase();
            processedEmails.add(mail);

            // Парсим DN: CN=ФИО,OU=Дивижин,OU=Департамент,OU=ФЦПСР,DC=...
            String[] parts = parseDN(dn);
            String deptName = parts.length >= 2 ? parts[1] : "Без департамента";
            String divName = parts.length >= 1 ? parts[0] : "Без подразделения";

            // Находим/создаём департамент в результате
            Map<String, Object> dept = deptMap.computeIfAbsent(deptName, k -> {
                Map<String, Object> d = new LinkedHashMap<>();
                d.put("title", k);
                d.put("status", "FROM_AD");
                d.put("divisions", new ArrayList<Map<String, Object>>());
                deptList.add(d);
                return d;
            });

            // Находим/создаём дивижин
            List<Map<String, Object>> divisions = (List<Map<String, Object>>) dept.get("divisions");
            Map<String, Object> division = null;
            for (Map<String, Object> d : divisions) {
                if (divName.equals(d.get("title"))) { division = d; break; }
            }
            if (division == null) {
                division = new LinkedHashMap<>();
                division.put("title", divName);
                division.put("status", "FROM_AD");
                division.put("employees", new ArrayList<Map<String, Object>>());
                divisions.add(division);
            }

            List<Map<String, Object>> employees = (List<Map<String, Object>>) division.get("employees");

            // Статус сотрудника
            Employee dbEmp = dbByEmail.get(mail);
            Map<String, Object> empEntry = new LinkedHashMap<>();
            empEntry.put("fullName", ad.get("cn"));
            empEntry.put("email", ad.get("mail"));
            empEntry.put("adTitle", ad.get("title"));
            empEntry.put("adDept", deptName);
            empEntry.put("adDiv", divName);

            if (dbEmp == null) {
                empEntry.put("status", "NEW");        // + новый из AD
            } else {
                String adFull = ((String) ad.get("sn")) + " " + ((String) ad.get("givenName"));
                String dbFull = dbEmp.getLastname() + " " + dbEmp.getName();
                if (adFull.equals(dbFull)) {
                    empEntry.put("status", "MATCH");   // ✓ совпадает
                } else {
                    empEntry.put("status", "UPDATED"); // ~ будет обновлён
                    empEntry.put("dbFullName", dbEmp.getLastname() + " " + dbEmp.getName() + " " + dbEmp.getMiddleName());
                }
                empEntry.put("dbTitle", dbEmp.getPosition());
            }
            employees.add(empEntry);
        }

        // 5. Непривязанные (есть в БД, нет в AD)
        List<Map<String, Object>> unlinked = new ArrayList<>();
        for (Map.Entry<String, Employee> entry : dbByEmail.entrySet()) {
            if (!processedEmails.contains(entry.getKey())) {
                Employee emp = entry.getValue();
                Map<String, Object> u = new LinkedHashMap<>();
                u.put("fullName", emp.getLastname() + " " + emp.getName() + " " + emp.getMiddleName());
                u.put("email", emp.getEmail());
                u.put("position", emp.getPosition());
                u.put("departmentId", emp.getDepartmentId());
                u.put("divisionId", emp.getDivisionId());
                u.put("status", "REMOVED"); // − нет в AD
                unlinked.add(u);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("departments", deptList);
        result.put("unlinked", unlinked);
        result.put("stats", Map.of(
            "adTotal", adUsers.size(),
            "dbTotal", dbEmployees.size(),
            "unlinkedTotal", unlinked.size()
        ));
        return result;
    }

    // ================================================================
    // Реальная синхронизация — ПИШЕТ в БД
    // ================================================================

    public Map<String, Object> syncAll() {
        // 0. Сохраняем порядок и сбрасываем структуру
        List<Employee> dbEmployees = employeeRepository.findAll().collectList().block();
        if (dbEmployees == null) dbEmployees = List.of();

        // Сохраняем порядок департаментов и дивижинов перед удалением
        List<Department> oldDepts = departmentRepository.findAll().collectList().block();
        List<Division> oldDivs = divisionRepository.findAll().collectList().block();
        Map<String, Integer> deptOrder = new HashMap<>();  // title(lower) → number
        Map<String, Integer> divOrder = new HashMap<>();   // title(lower)+"|"+deptId → number
        if (oldDepts != null) {
            for (Department d : oldDepts) {
                if (d.getTitle() != null) deptOrder.put(d.getTitle().toLowerCase(), d.getNumber());
            }
        }
        if (oldDivs != null) {
            for (Division d : oldDivs) {
                if (d.getTitle() != null) divOrder.put(d.getTitle().toLowerCase() + "|" + d.getDepartmentId(), d.getNumber());
            }
        }

        // Открепляем сотрудников
        for (Employee emp : dbEmployees) {
            if (emp.getDepartmentId() > 0 || emp.getDivisionId() > 0) {
                emp.setDepartmentId(0);
                emp.setDivisionId(0);
                employeeRepository.save(emp).subscribe();
            }
        }
        // Удаляем дивижины и департаменты
        divisionRepository.deleteAll().block();
        departmentRepository.deleteAll().block();
        log.info("Сброшено: {} сотрудников, департаменты и дивижины удалены", dbEmployees.size());

        // 1. Все активные из AD (обязательно с телефоном и почтой)
        List<Map<String, Object>> adUsers;
        try {
            adUsers = ldapTemplate.search(baseDn,
                "(&(objectCategory=person)(mail=*)(telephoneNumber=*)(|(&(objectClass=user)(givenName=*)(sn=*)(!(userAccountControl:1.2.840.113556.1.4.803:=2)))(objectClass=contact)))",
                new AdUserMapper());
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }

        // 2. Все департаменты и дивижины из БД
        List<Department> allDepts = departmentRepository.findAll().collectList().block();
        List<Division> allDivs = divisionRepository.findAll().collectList().block();
        if (allDepts == null) allDepts = List.of();
        if (allDivs == null) allDivs = List.of();

        // 3. Все сотрудники из БД (уже загружены в dbEmployees на шаге 0)
        // dbEmployees уже содержит всех — не загружаем повторно

        Set<String> adEmails = new HashSet<>();
        int created = 0, updated = 0, unlinked = 0;

        for (Map<String, Object> ad : adUsers) {
            String mail = ((String) ad.get("mail")).toLowerCase();
            adEmails.add(mail);

            String dn = (String) ad.get("dn");
            String[] ous = parseDN(dn);

            // Пользователь только в ФЦПСР → отвязать
            if (ous.length == 0) {
                Employee emp = findByEmail(dbEmployees, mail);
                if (emp != null && (emp.getDepartmentId() > 0 || emp.getDivisionId() > 0)) {
                    emp.setDepartmentId(0);
                    emp.setDivisionId(0);
                    employeeRepository.save(emp).subscribe();
                    unlinked++;
                }
                continue;
            }

            // Берём первые два OU (ближайшие к пользователю), остальные откидываем
            // Если OU >= 2: ou[0]=дивижин, ou[1]=департамент
            // Если OU == 1: департамент, дивижина нет
            String divName = ous.length >= 2 ? normalize(ous[0]) : null;
            String deptName = ous.length >= 2 ? normalize(ous[1]) : normalize(ous[0]);

            // Департамент
            Department dept = findOrCreateDept(allDepts, deptName, deptOrder);
            // Дивижин (только если есть)
            Division div = divName != null ? findOrCreateDiv(allDivs, divName, dept.getId(), divOrder) : null;

            // Сотрудник
            Employee emp = findByEmail(dbEmployees, mail);
            String[] cn = parseCN((String) ad.get("cn"));

            if (emp == null) {
                emp = new Employee();
                emp.setLastname(cn[0]);
                emp.setName(cn[1]);
                emp.setMiddleName(cn[2]);
                emp.setPosition(normalize((String) ad.get("title")));
                emp.setPhone((String) ad.get("telephoneNumber"));
                emp.setEmail((String) ad.get("mail"));
                String otherPhone = (String) ad.getOrDefault("otherTelephone", "");
                if (otherPhone == null || otherPhone.isBlank()) {
                    otherPhone = (String) ad.getOrDefault("mobile", "");
                }
                if (otherPhone != null && !otherPhone.isBlank()) {
                    emp.setPersonalPhone(otherPhone);
                }
                emp.setCabinet("—");
                emp.setAddress("—");
                if (div != null) {
                    emp.setDivisionId(div.getId());
                    emp.setDepartmentId(0);
                } else {
                    emp.setDepartmentId(dept.getId());
                    emp.setDivisionId(0);
                }
                // Вычисляем следующий номер в дивижине/департаменте
                final long finalDivId = div != null ? div.getId() : 0;
                final long finalDeptId = dept.getId();
                int maxNum = dbEmployees.stream().filter(e -> {
                    if (div != null) return e.getDivisionId() == finalDivId;
                    else return e.getDepartmentId() == finalDeptId;
                }).mapToInt(Employee::getNumber).max().orElse(0);
                emp.setNumber(maxNum + 1);
                employeeRepository.save(emp).subscribe();
                dbEmployees.add(emp); // добавляем в локальный список
                created++;
            } else {
                emp.setLastname(cn[0]);
                emp.setName(cn[1]);
                emp.setMiddleName(cn[2]);
                emp.setPosition(normalize((String) ad.get("title")));
                emp.setPhone((String) ad.get("telephoneNumber"));
                emp.setEmail((String) ad.get("mail"));
                String otherPhone = (String) ad.getOrDefault("otherTelephone", "");
                if (otherPhone == null || otherPhone.isBlank()) {
                    otherPhone = (String) ad.getOrDefault("mobile", "");
                }
                if (otherPhone != null && !otherPhone.isBlank()) {
                    emp.setPersonalPhone(otherPhone);
                }
                if (div != null) { emp.setDivisionId(div.getId()); emp.setDepartmentId(0); }
                else { emp.setDepartmentId(dept.getId()); emp.setDivisionId(0); }
                employeeRepository.save(emp).subscribe();
                updated++;
            }
        }

        // 4. Открепление отсутствующих + установка INACTIVE в employee_job_system
        for (Employee emp : dbEmployees) {
            if ((emp.getDepartmentId() > 0 || emp.getDivisionId() > 0)
                && emp.getEmail() != null && !adEmails.contains(emp.getEmail().toLowerCase())) {
                emp.setDepartmentId(0);
                emp.setDivisionId(0);
                employeeRepository.save(emp).subscribe();
                // Делаем неактивными все привязки к системам
                databaseClient.sql("UPDATE employee_job_system SET status = 'INACTIVE', disconnect_date = :now WHERE employee_id = :empId AND status = 'ACTIVE'")
                    .bind("empId", emp.getId())
                    .bind("now", java.time.LocalDate.now())
                    .fetch().rowsUpdated().subscribe();
                unlinked++;
            }
        }

        return Map.of(
            "adTotal", adUsers.size(),
            "created", created,
            "updated", updated,
            "unlinked", unlinked
        );
    }

    private String normalize(String s) {
        if (s == null || s.isBlank()) return "";
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return "";
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1).toLowerCase();
    }

    private String[] parseCN(String cn) {
        if (cn == null) return new String[]{"—", "—", "—"};
        String[] parts = cn.trim().split("\\s+");
        return new String[]{
            parts.length > 0 ? normalize(parts[0]) : "—",
            parts.length > 1 ? normalize(parts[1]) : "—",
            parts.length > 2 ? normalize(parts[2]) : "—"
        };
    }

    private Employee findByEmail(List<Employee> employees, String mail) {
        for (Employee e : employees) {
            if (e.getEmail() != null && e.getEmail().equalsIgnoreCase(mail)) return e;
        }
        return null;
    }

    private Department findOrCreateDept(List<Department> depts, String title, Map<String, Integer> order) {
        String lower = title.toLowerCase();
        for (Department d : depts) {
            if (d.getTitle() != null && d.getTitle().toLowerCase().equals(lower)) return d;
        }
        Department d = new Department();
        d.setTitle(title);
        d.setDescription(title);
        // Восстанавливаем старый порядок, или добавляем в конец
        Integer oldNum = order.get(lower);
        if (oldNum != null) {
            d.setNumber(oldNum);
        } else {
            int maxNum = depts.stream().mapToInt(de -> de.getNumber()).max().orElse(0);
            d.setNumber(maxNum + 1);
        }
        Department saved = departmentRepository.save(d).block();
        depts.add(saved);
        return saved;
    }

    private Division findOrCreateDiv(List<Division> divs, String title, long deptId, Map<String, Integer> order) {
        String lower = title.toLowerCase();
        for (Division d : divs) {
            if (d.getTitle() != null && d.getTitle().toLowerCase().equals(lower)
                && d.getDepartmentId() == deptId) return d;
        }
        Division d = new Division();
        d.setTitle(title);
        d.setDescription(title);
        d.setDepartmentId(deptId);
        Integer oldNum = order.get(lower + "|" + deptId);
        if (oldNum != null) {
            d.setNumber(oldNum);
        } else {
            int maxNum = divs.stream().filter(dv -> dv.getDepartmentId() == deptId).mapToInt(dv -> dv.getNumber()).max().orElse(0);
            d.setNumber(maxNum + 1);
        }
        Division saved = divisionRepository.save(d).block();
        divs.add(saved);
        return saved;
    }

    /**
     * Парсит Distinguished Name, извлекает цепочку OU (от ближайшего к дальнему).
     * "CN=Иван,OU=Служба ИБ,OU=УИТ,OU=ФЦПСР,DC=..." → ["Служба ИБ", "УИТ"]
     * Обрабатывает экранированные символы: \\,  \\=  \\+  и т.д.
     */
    private String[] parseDN(String dn) {
        if (dn == null) return new String[0];
        List<String> ous = new ArrayList<>();
        // Разбиваем DN на RDN (части, разделённые запятыми, с учётом экранирования)
        List<String> rdns = splitRDN(dn);
        for (String rdn : rdns) {
            rdn = rdn.trim();
            // Отменяем экранирование: \, → ,  \= → =  \\ → \
            rdn = rdn.replace("\\,", ",").replace("\\=", "=").replace("\\\\", "\\");
            if (rdn.startsWith("OU=")) {
                String val = rdn.substring(3);
                if (!"ФЦПСР".equalsIgnoreCase(val)) {
                    ous.add(val);
                }
            }
        }
        // Не переворачиваем: в DN порядок от ближнего OU к дальнему
        return ous.toArray(new String[0]);
    }

    private List<String> splitRDN(String dn) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < dn.length(); i++) {
            if (dn.charAt(i) == ',' && (i == 0 || dn.charAt(i - 1) != '\\')) {
                parts.add(dn.substring(start, i));
                start = i + 1;
            }
        }
        if (start < dn.length()) {
            parts.add(dn.substring(start));
        }
        return parts;
    }
}
