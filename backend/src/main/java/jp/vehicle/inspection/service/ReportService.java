package jp.vehicle.inspection.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jp.vehicle.inspection.domain.entity.Invoice;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class ReportService {

  public byte[] generateInvoicePdf(Invoice invoice) throws Exception {
    String html =
        """
        <!DOCTYPE html><html><head><meta charset="UTF-8"/><style>
        body{font-family:sans-serif;padding:24px}h1{font-size:18px}
        table{width:100%;border-collapse:collapse;margin-top:16px}
        td,th{border:1px solid #ccc;padding:8px;text-align:left}
        </style></head><body>
        <h1>請求書 %s</h1>
        <p>発行日: %s</p>
        <table><tr><th>項目</th><th>金額</th></tr>
        <tr><td>小計</td><td>%,.0f 円</td></tr>
        <tr><td>消費税</td><td>%,.0f 円</td></tr>
        <tr><td><strong>合計</strong></td><td><strong>%,.0f 円</strong></td></tr>
        </table></body></html>
        """
            .formatted(
                invoice.getInvoiceNumber(),
                invoice.getIssuedAt(),
                invoice.getSubtotal(),
                invoice.getTaxAmount(),
                invoice.getTotalAmount());

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      PdfRendererBuilder builder = new PdfRendererBuilder();
      builder.withHtmlContent(html, null);
      builder.toStream(out);
      builder.run();
      return out.toByteArray();
    }
  }

  public byte[] generateInvoiceExcel(Invoice invoice) throws Exception {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("請求書");
      Row r0 = sheet.createRow(0);
      r0.createCell(0).setCellValue("請求書番号");
      r0.createCell(1).setCellValue(invoice.getInvoiceNumber());
      Row r1 = sheet.createRow(1);
      r1.createCell(0).setCellValue("発行日");
      r1.createCell(1).setCellValue(invoice.getIssuedAt().toString());
      Row r2 = sheet.createRow(3);
      r2.createCell(0).setCellValue("小計");
      r2.createCell(1).setCellValue(invoice.getSubtotal().doubleValue());
      Row r3 = sheet.createRow(4);
      r3.createCell(0).setCellValue("消費税");
      r3.createCell(1).setCellValue(invoice.getTaxAmount().doubleValue());
      Row r4 = sheet.createRow(5);
      r4.createCell(0).setCellValue("合計");
      r4.createCell(1).setCellValue(invoice.getTotalAmount().doubleValue());
      workbook.write(out);
      return out.toByteArray();
    }
  }
}
