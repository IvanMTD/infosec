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
@RequestMapping("/contacts")
public class XMLController2 {

    private final DepartmentRepository departmentRepository;
    private final DivisionRepository divisionRepository;
    private final EmployeeService employeeService;

    @GetMapping(path = "/group/list.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public Mono<String> getStructuredContacts() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<YealinkIPPhoneBook>");
        xml.append("<Title>Yealink</Title>");
        return departmentRepository.findAll().flatMap(department -> {
            return employeeService.getAllByDepartmentId(department.getId()).collectList().flatMap(employees -> {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("<Menu Name=\"" + department.getTitle() + "\">");
                for(Employee employee : employees){
                    stringBuilder.append("<Unit Name=\"")
                            .append(employee.getFullName())
                            .append("\" Phone1=\"").append(employee.getPhone())
                            .append("\" Phone2=\"").append(employee.getPersonalPhone())
                            .append("\" Phone3=\"\"") // Исправлено: добавлен закрывающий символ `>`
                            .append(" default_photo=\"Resource:https://imgcdn.stablediffusionweb.com/2024/3/5/3531a5be-ca54-454d-b270-d069d92a707f.jpg\" />");
                }
                stringBuilder.append("</Menu>");
                return Mono.just(stringBuilder.toString());
            });
        }).collectList().flatMap(list -> {
            for(String string : list){
                xml.append(string);
            }
            return divisionRepository.findAll().flatMap(division -> {
                return employeeService.getAllByDivisionId(division.getId()).collectList().flatMap(employees -> {
                    // Формируем XML для каждого подразделения
                    StringBuilder divisionXml = new StringBuilder();
                    divisionXml.append("<Menu Name=\"").append(division.getTitle()).append("\">");

                    // Перебираем сотрудников и формируем XML для каждого
                    for(Employee employee : employees) {
                        String[] parts = employee.getPersonalPhone().split("  ");
                        String phone2 = "";
                        String phone3 = "";
                        if(parts.length == 2){
                            phone2 = parts[0];
                            phone3 = parts[1];
                        }else{
                            phone2 = employee.getPersonalPhone() != null ? employee.getPersonalPhone() : "";
                        }

                        divisionXml.append("<Unit Name=\"")
                                .append(employee.getFullName())
                                .append("\" Phone1=\"").append(employee.getPhone())
                                .append("\" Phone2=\"").append(phone2)
                                .append("\" Phone3=\"").append(phone3)  // Исправлено: добавлен пробел перед следующим атрибутом
                                .append("\" default_photo=\"Resource:https://imgcdn.stablediffusionweb.com/2024/3/5/3531a5be-ca54-454d-b270-d069d92a707f.jpg\" />");
                    }
                    divisionXml.append("</Menu>"); // Закрываем тег Menu для каждого подразделения
                    return Mono.just(divisionXml.toString());
                });
            }).collectList().flatMap(list2 -> {
                // Добавляем XML всех подразделений в основной XML
                for(String divisionXml : list2) {
                    xml.append(divisionXml);
                }
                xml.append("</YealinkIPPhoneBook>"); // Закрываем корневой тег
                System.out.println(xml);
                return Mono.just(xml.toString());
            });
        });
    }
}
