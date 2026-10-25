package com.chaitanya.email_sender.Service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {

    public List<String> emails(MultipartFile file) throws IOException {

        List<String> emails = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new IllegalArgumentException("Excel header row is missing");
            }

            int emailColumn = -1;

            for (Cell cell : headerRow) {
                if (cell.getCellType() == CellType.STRING &&
                        ("Email".equalsIgnoreCase(cell.getStringCellValue().trim())||
                                "Emails".equalsIgnoreCase(cell.getStringCellValue().trim()))){

                    emailColumn = cell.getColumnIndex();
                    break;
                }
            }

            if (emailColumn == -1) {
                throw new IllegalArgumentException(
                        "Email column not found in Excel file"
                );
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                Cell cell = row.getCell(emailColumn);

                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String email = cell.getStringCellValue().trim();

                    if (!email.isEmpty()) {
                        emails.add(email);
                    }
                }
            }
        }

        return emails;
    }
}