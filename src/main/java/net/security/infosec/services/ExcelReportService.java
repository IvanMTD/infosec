package net.security.infosec.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.security.infosec.models.entity.Category;
import net.security.infosec.models.entity.Task;
import net.security.infosec.models.entity.Trouble;
import net.security.infosec.repositories.TaskRepository;
import net.security.infosec.repositories.TroubleRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelReportService {

    private final TaskRepository taskRepository;
    private final TroubleTicketService troubleTicketService;
    private final TroubleRepository troubleRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Mono<byte[]> generateReport(String mode, int userId, String dateFrom, String dateTo) {
        LocalDate now = LocalDate.now();
        LocalDate from, to;
        var wf = WeekFields.of(java.util.Locale.getDefault());
        int week = now.get(wf.weekOfWeekBasedYear());

        switch (mode) {
            case "week":
                from = now.with(wf.weekOfWeekBasedYear(), week).with(wf.dayOfWeek(), 1);
                to = from.plusDays(6);
                break;
            case "month":
                from = now.withDayOfMonth(1);
                to = now.withDayOfMonth(now.lengthOfMonth());
                break;
            case "year":
                from = now.withDayOfYear(1);
                to = now.withDayOfYear(now.lengthOfYear());
                break;
            case "date":
                from = (dateFrom != null && !dateFrom.isEmpty()) ? LocalDate.parse(dateFrom) : now.minusDays(7);
                to = (dateTo != null && !dateTo.isEmpty()) ? LocalDate.parse(dateTo) : now;
                break;
            default:
                from = now.withDayOfYear(1);
                to = now;
        }

        String periodLabel = buildPeriodLabel(mode, dateFrom, dateTo, from, to, now, wf, week);

        return loadTasks(from, to, userId)
            .flatMap(tasks -> troubleTicketService.getTroubleCategoryMap().flatMap(troubleCategoryMap ->
                troubleTicketService.getAllCategories().collectList().flatMap(categories ->
                    troubleRepository.findAll().collectList().map(allTroubles ->
                        buildExcel(tasks, categories, allTroubles, troubleCategoryMap, periodLabel)
                    )
                )
            ));
    }

    private Mono<List<Task>> loadTasks(LocalDate from, LocalDate to, int userId) {
        if (userId <= 0) {
            return taskRepository.findAllByExecuteDateBetween(from, to).collectList();
        } else {
            return taskRepository.findAllByImplementerId(userId)
                .filter(t -> {
                    LocalDate d = t.getExecuteDate();
                    return d != null && !d.isBefore(from) && !d.isAfter(to);
                })
                .collectList();
        }
    }

    private byte[] buildExcel(List<Task> tasks, List<Category> categories, List<Trouble> allTroubles,
                               Map<Integer, Integer> troubleCategoryMap, String periodLabel) {
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle cellStyle = createCellStyle(wb);

            // Карты: id → name
            Map<Integer, String> troubleNameMap = new HashMap<>();
            Map<Integer, String> categoryNameMap = new HashMap<>();
            for (Trouble t : allTroubles) troubleNameMap.put(t.getId(), t.getName());
            for (Category c : categories) categoryNameMap.put(c.getId(), c.getName());

            // ── Лист 1: Сводка ──
            Sheet summary = wb.createSheet("Сводка");
            int rowIdx = 0;

            Row periodRow = summary.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("Период:");
            periodRow.getCell(0).setCellStyle(headerStyle);
            periodRow.createCell(1).setCellValue(periodLabel);
            periodRow.getCell(1).setCellStyle(cellStyle);
            rowIdx++;

            Row header = summary.createRow(rowIdx++);
            String[] cols = {"Категория", "Проблема", "Количество"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i); c.setCellValue(cols[i]); c.setCellStyle(headerStyle);
            }

            // Группировка: categoryName → troubleName → count
            Map<String, Map<String, Integer>> grouped = new LinkedHashMap<>();
            for (Category cat : categories) {
                Map<String, Integer> tm = new LinkedHashMap<>();
                for (Trouble t : allTroubles) {
                    if (t.getCategoryId() == cat.getId()) tm.put(t.getName(), 0);
                }
                if (!tm.isEmpty()) grouped.put(cat.getName(), tm);
            }

            for (Task task : tasks) {
                String troubleName = troubleNameMap.get(task.getTroubleId());
                Integer catId = troubleCategoryMap.get(task.getTroubleId());
                String catName = catId != null ? categoryNameMap.get(catId) : null;
                if (catName != null && troubleName != null && grouped.containsKey(catName)) {
                    Map<String, Integer> tm = grouped.get(catName);
                    if (tm.containsKey(troubleName)) tm.put(troubleName, tm.get(troubleName) + 1);
                }
            }

            for (Map.Entry<String, Map<String, Integer>> catEntry : grouped.entrySet()) {
                for (Map.Entry<String, Integer> trEntry : catEntry.getValue().entrySet()) {
                    if (trEntry.getValue() == 0) continue;
                    Row r = summary.createRow(rowIdx++);
                    r.createCell(0).setCellValue(catEntry.getKey()); r.getCell(0).setCellStyle(cellStyle);
                    r.createCell(1).setCellValue(trEntry.getKey()); r.getCell(1).setCellStyle(cellStyle);
                    r.createCell(2).setCellValue(trEntry.getValue()); r.getCell(2).setCellStyle(cellStyle);
                }
            }
            for (int i = 0; i < cols.length; i++) summary.autoSizeColumn(i);

            // ── Лист 2: Детализация ──
            Sheet detail = wb.createSheet("Детализация");
            int dRow = 0;
            Row dHeader = detail.createRow(dRow++);
            String[] dCols = {"Категория", "Проблема", "Заголовок", "Описание", "Дата выполнения"};
            for (int i = 0; i < dCols.length; i++) {
                Cell c = dHeader.createCell(i); c.setCellValue(dCols[i]); c.setCellStyle(headerStyle);
            }

            for (Task task : tasks) {
                String troubleName = troubleNameMap.get(task.getTroubleId());
                Integer catId = troubleCategoryMap.get(task.getTroubleId());
                String catName = catId != null ? categoryNameMap.get(catId) : "";

                Row r = detail.createRow(dRow++);
                r.createCell(0).setCellValue(catName); r.getCell(0).setCellStyle(cellStyle);
                r.createCell(1).setCellValue(troubleName != null ? troubleName : ""); r.getCell(1).setCellStyle(cellStyle);
                r.createCell(2).setCellValue(task.getTitle() != null ? task.getTitle() : ""); r.getCell(2).setCellStyle(cellStyle);
                r.createCell(3).setCellValue(task.getDescription() != null ? task.getDescription() : ""); r.getCell(3).setCellStyle(cellStyle);
                r.createCell(4).setCellValue(task.getExecuteDate() != null ? task.getExecuteDate().format(DATE_FMT) : "");
                r.getCell(4).setCellStyle(cellStyle);
            }
            for (int i = 0; i < dCols.length; i++) detail.autoSizeColumn(i);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Excel generation error", e);
            return new byte[0];
        }
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); s.setFont(f);
        for (BorderStyle bs : new BorderStyle[]{BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN}) {
            s.setBorderBottom(bs); s.setBorderTop(bs); s.setBorderLeft(bs); s.setBorderRight(bs);
        }
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private CellStyle createCellStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setBorderBottom(BorderStyle.THIN); s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN); s.setBorderRight(BorderStyle.THIN);
        s.setWrapText(true);
        return s;
    }

    private String buildPeriodLabel(String mode, String dateFrom, String dateTo,
                                     LocalDate from, LocalDate to, LocalDate now,
                                     WeekFields wf, int week) {
        switch (mode) {
            case "week":
                return "с " + from.format(DATE_FMT) + " по " + to.format(DATE_FMT) + " (неделя " + week + ")";
            case "month":
                return "с " + from.format(DATE_FMT) + " по " + to.format(DATE_FMT);
            case "year":
                return "с 01.01." + now.getYear() + " по 31.12." + now.getYear();
            default:
                return "с " + from.format(DATE_FMT) + " по " + to.format(DATE_FMT);
        }
    }
}
