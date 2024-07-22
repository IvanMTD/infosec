package net.security.infosec.configurations;

import lombok.extern.slf4j.Slf4j;
import net.security.infosec.models.Employee;
import net.security.infosec.repositories.DepartmentRepository;
import net.security.infosec.repositories.DivisionRepository;
import net.security.infosec.repositories.EmployeeRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

@Slf4j
@Configuration
public class ApplicationConfig {
    @Bean
    public CommandLineRunner preLoad(DepartmentRepository departmentRepository, DivisionRepository divisionRepository, EmployeeRepository employeeRepository){
        return args -> {
            //setupEmployees(employeeRepository);
            //numerate(departmentRepository,divisionRepository,employeeRepository);
        };
    }

    private void numerate(DepartmentRepository departmentRepository, DivisionRepository divisionRepository, EmployeeRepository employeeRepository){
        departmentRepository.findAll().flatMap(department -> {
            return employeeRepository.findByDepartmentId(department.getId()).collectList().flatMapMany(employees -> {
                int n = 1;
                for(Employee employee : employees){
                    employee.setNumber(n);
                    n++;
                }
                return Flux.fromIterable(employees);
            }).flatMap(employee -> {
                return employeeRepository.save(employee);
            }).collectList().flatMap(l -> {
                log.info("employee saved {}", l);
                return Mono.just(department);
            });
        }).flatMap(department -> {
            return divisionRepository.findAllByIdIn(department.getDivisionIds()).flatMap(division -> {
                return employeeRepository.findByDivisionId(division.getId()).collectList().flatMapMany(employees -> {
                    int n = 1;
                    for(Employee employee : employees){
                        employee.setNumber(n);
                        n++;
                    }
                    return Flux.fromIterable(employees);
                }).flatMap(employee -> {
                    return employeeRepository.save(employee);
                }).collectList().flatMap(l -> {
                    log.info("employees saved: {}", l);
                    return Mono.just(division);
                });
            });
        }).subscribe();
    }

    private void setupEmployees(EmployeeRepository employeeRepository){
        log.info("process \"setup employees\" start");
        long ms = System.currentTimeMillis();
        XSSFWorkbook wb = getWorkBookFromXSSF("./src/main/resources/static/files/guid.xlsx");
        XSSFSheet sheet = Objects.requireNonNull(wb).getSheet("Лист1");
        Iterator<Row> rowIter = sheet.rowIterator();
        while (rowIter.hasNext()) {
            Row row = rowIter.next();
            Cell fullName = row.getCell(0);
            Cell position = row.getCell(1);
            Cell shortNumber = row.getCell(3);
            Cell personalPhone = row.getCell(4);
            Cell email = row.getCell(5);

            String trimName = fullName.toString().trim();
            String[] parts = trimName.split(" ");
            String lastname = parts[0];
            String name = parts[1];
            String middleName = parts[2];

            String trimPosition = position.toString().trim();
            String firstLetter = trimPosition.substring(0, 1).toUpperCase();
            String restOfString = trimPosition.substring(1);
            String result = firstLetter + restOfString;

            String trimNumber = shortNumber.toString().trim();
            String phoneNumber = "0";
            if(!trimNumber.equals("-")){
                String data = trimNumber.substring(0,trimNumber.length() - 2);
                phoneNumber = data;
            }
            String trimPersonalPhone = personalPhone.toString().trim();

            String trimEmail = email.toString().trim();

            String finalPhoneNumber = phoneNumber;
            employeeRepository.findByLastnameIgnoreCaseAndNameIgnoreCaseAndMiddleNameIgnoreCase(lastname,name,middleName).flatMap(employee -> {
                employee.setLastname(lastname);
                employee.setName(name);
                employee.setMiddleName(middleName);
                employee.setPosition(result);
                employee.setPhone(finalPhoneNumber);
                employee.setPersonalPhone(trimPersonalPhone);
                employee.setEmail(trimEmail);
                return employeeRepository.save(employee);
            }).switchIfEmpty(
                    Mono.just(new Employee()).flatMap(employee -> {
                        employee.setLastname(lastname);
                        employee.setName(name);
                        employee.setMiddleName(middleName);
                        employee.setPosition(result);
                        employee.setPhone(finalPhoneNumber);
                        employee.setPersonalPhone(trimPersonalPhone);
                        employee.setEmail(trimEmail);
                        return employeeRepository.save(employee);
                    })
            ).subscribe();
        }
        try {
            wb.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("process \"setup employees\" end for {} ms", System.currentTimeMillis() - ms);
    }

    private XSSFWorkbook getWorkBookFromXSSF(String filePath){
        try{
            return new XSSFWorkbook(new FileInputStream(filePath));
        }catch (Exception e){
            log.error("process XSSFWorkbook end with error {}", e.getMessage());
            return null;
        }
    }

    private HSSFWorkbook getWorkBookFromHSSF(String filePath){
        try{
            return new HSSFWorkbook(new FileInputStream(filePath));
        }catch (Exception e){
            log.error("process HSSFWorkbook end with error {}", e.getMessage());
            return null;
        }
    }
}
