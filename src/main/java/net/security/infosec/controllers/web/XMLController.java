package net.security.infosec.controllers.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.services.EmployeeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/contacts.xml")
public class XMLController {

    private final EmployeeService employeeService;

    /*@GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public Mono<String> getContacts() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        xml.append("<root>");
        xml.append("<root_group>");
        xml.append("<group display_name=\"All Contacts\" />");
        xml.append("<group display_name=\"Blocklist\" />");
        xml.append("</root_group>");
        xml.append("<root_contact>");

        return employeeService.getAllActual()
                .doOnNext(employee -> {
                    xml.append("<contact display_name=\"")
                            .append(employee.getLastname()).append(" ")
                            .append(employee.getName()).append(" ")
                            .append(employee.getMiddleName())
                            .append("\" office_number=\"").append(employee.getPhone())
                            .append("\" line=\"-1\" ring=\"Auto\" default_photo=\"\" selected_photo=\"0\" group_id_name=\"All Contacts\" eyepea_contact_id=\"\" />");
                })
                .then(Mono.fromSupplier(() -> {
                    xml.append("</root_contact>");
                    xml.append("</root>");
                    return xml.toString();
                }));
    }*/

    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    //public Mono<ResponseEntity<String>> getContacts() {
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
                /*.then(Mono.fromSupplier(() -> {
                    xml.append("</Menu>");
                    xml.append("</YealinkIPPhoneBook>");

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contacts.xml\"")
                            .contentType(MediaType.APPLICATION_XML)
                            .body(xml.toString());
                }));*/
    }

}
