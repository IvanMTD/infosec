package net.security.infosec.controllers.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.models.Employee;
import net.security.infosec.repositories.DepartmentRepository;
import net.security.infosec.repositories.DivisionRepository;
import net.security.infosec.services.EmployeeService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/contacts.xml")
public class XMLController {
    private final EmployeeService employeeService;

    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public Mono<String> getContacts() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<YealinkIPPhoneBook>");
        xml.append("<Title>Yealink</Title>");
        xml.append("<Menu Name=\"Все\">");
        return employeeService.getAllActual()
                .doOnNext(employee -> {
                    xml.append("<Unit Name=\"")
                            .append(employee.getLastname()).append(" ")
                            .append(employee.getName()).append(" ")
                            .append(employee.getMiddleName())
                            .append("\" Phone1=\"").append(employee.getPhone())
                            .append("\" Phone2=\"").append(employee.getPersonalPhone())
                            .append("\" Phone3=\"\"") // Исправлено: добавлен закрывающий символ `>`
                            .append(" default_photo=\"Resource:null\" />");
                })
                .then(Mono.fromSupplier(() -> {
                    xml.append("</Menu>");
                    xml.append("</YealinkIPPhoneBook>");
                    return xml.toString();
                }));
    }

    /*@GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public Mono<ResponseEntity<String>> downloadContacts() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<YealinkIPPhoneBook>");
        xml.append("<Title>Yealink</Title>");
        xml.append("<Menu Name=\"Все\">");
        return employeeService.getAllActual()
                .doOnNext(employee -> {
                    xml.append("<Unit Name=\"")
                            .append(employee.getLastname()).append(" ")
                            .append(employee.getName()).append(" ")
                            .append(employee.getMiddleName())
                            .append("\" Phone1=\"").append(employee.getPhone())
                            .append("\" Phone2=\"").append(employee.getPersonalPhone())
                            .append("\" Phone3=\"\"") // Исправлено: добавлен закрывающий символ `>`
                            .append(" default_photo=\"Resource:null\" />");
                })
                .then(Mono.fromSupplier(() -> {
                    xml.append("</Menu>");
                    xml.append("</YealinkIPPhoneBook>");

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contacts.xml\"")
                            .contentType(MediaType.APPLICATION_XML)
                            .body(xml.toString());
                }));
    }*/
}
