package com.group7.marketplacesystem.commerce.report.service.impl;

import com.group7.marketplacesystem.commerce.report.dto.response.RevenueByPeriodDTO;
import com.group7.marketplacesystem.commerce.report.dto.response.RevenueBySellerDTO;
import com.group7.marketplacesystem.commerce.report.dto.response.RevenueReportResponse;
import com.group7.marketplacesystem.commerce.report.dto.response.TopRevenueDayDTO;
import com.group7.marketplacesystem.commerce.report.service.PdfExportService;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfExportServiceImpl implements PdfExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    @Override
    public ByteArrayOutputStream generatePdfReport(RevenueReportResponse report) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
//            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
//            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            PdfFont font = PdfFontFactory.createFont(
                    "src/main/resources/fonts/DejaVuSans.ttf",
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );

            PdfFont boldFont = PdfFontFactory.createFont(
                    "src/main/resources/fonts/DejaVuSans.ttf",
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );

            // Title
            Paragraph title = new Paragraph("BÁO CÁO DOANH THU")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Period info
            Paragraph periodInfo = new Paragraph()
                    .setFont(font)
                    .setFontSize(10)
                    .add("Từ ngày: " + report.getStartDate().format(DATE_FORMATTER))
                    .add("     Đến ngày: " + report.getEndDate().format(DATE_FORMATTER))
                    .setMarginBottom(15);
            document.add(periodInfo);

            // Summary section
            createSummarySection(document, report, font, boldFont);

            // Revenue by period section
            createRevenueByPeriodSection(document, report, font, boldFont);

            // Top revenue days section
            createTopRevenueDaysSection(document, report, font, boldFont);

            // Revenue by seller section (if available)
            if (!report.getRevenueBySeller().isEmpty()) {
                createRevenueBySellerSection(document, report, font, boldFont);
            }

            // Summary at the end
            createFinalSummary(document, report, font, boldFont);

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        } finally {
            document.close();
        }

        return baos;
    }

    private void createSummarySection(Document document, RevenueReportResponse report,
                                     PdfFont font, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("1. TỔNG QUAN")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        Table summaryTable = new Table(2)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);

        addTableHeader(summaryTable, "Chỉ tiêu", boldFont);
        addTableHeader(summaryTable, "Giá trị", boldFont);

        addTableRow(summaryTable, font, "Tổng doanh thu", formatCurrency(report.getTotalRevenue()));
        addTableRow(summaryTable, font,"Tổng số đơn hàng", String.valueOf(report.getTotalOrders()));
        addTableRow(summaryTable, font,  "Tổng tiền COD", formatCurrency(report.getTotalCodAmount()));
        addTableRow(summaryTable, font, "Tổng tiền Online", formatCurrency(report.getTotalOnlineAmount()));

        document.add(summaryTable);
    }

    private void createRevenueByPeriodSection(Document document, RevenueReportResponse report,
                                             PdfFont font, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("2. DOANH THU THEO KỲ")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        Table table = new Table(3)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);

        addTableHeader(table, "Kỳ", boldFont);
        addTableHeader(table, "Doanh thu", boldFont);
        addTableHeader(table, "Số đơn hàng", boldFont);

        for (RevenueByPeriodDTO dto : report.getRevenueByPeriod()) {
            addTableRow(table, font, dto.getPeriodLabel(),
                       formatCurrency(dto.getRevenue()), 
                       String.valueOf(dto.getOrderCount()));
        }

        document.add(table);
    }

    private void createTopRevenueDaysSection(Document document, RevenueReportResponse report,
                                            PdfFont font, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("3. TOP 10 NGÀY CÓ DOANH THU CAO NHẤT")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        Table table = new Table(4)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);

        addTableHeader(table, "Thứ hạng", boldFont);
        addTableHeader(table, "Ngày", boldFont);
        addTableHeader(table, "Doanh thu", boldFont);
        addTableHeader(table, "Số đơn hàng", boldFont);

        for (TopRevenueDayDTO dto : report.getTopRevenueDays()) {
            addTableRow(table, font,
                       String.valueOf(dto.getRank()),
                       dto.getDate().format(DATE_FORMATTER),
                       formatCurrency(dto.getRevenue()),
                       String.valueOf(dto.getOrderCount()));
        }

        document.add(table);
    }

    private void createRevenueBySellerSection(Document document, RevenueReportResponse report,
                                             PdfFont font, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("4. DOANH THU THEO SELLER")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(sectionTitle);

        Table table = new Table(7)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);

        addTableHeader(table, "Seller ID", boldFont);
        addTableHeader(table, "Tên shop", boldFont);
        addTableHeader(table, "Email", boldFont);
        addTableHeader(table, "Doanh thu", boldFont);
        addTableHeader(table, "Số đơn", boldFont);
        addTableHeader(table, "COD", boldFont);
        addTableHeader(table, "Online", boldFont);

        for (RevenueBySellerDTO dto : report.getRevenueBySeller()) {
            addTableRow(table, font,
                       String.valueOf(dto.getSellerId()),
                       dto.getSellerName(),
                       dto.getSellerEmail(),
                       formatCurrency(dto.getRevenue()),
                       String.valueOf(dto.getOrderCount()),
                       formatCurrency(dto.getCodAmount()),
                       formatCurrency(dto.getOnlineAmount()));
        }

        document.add(table);
    }

    private void createFinalSummary(Document document, RevenueReportResponse report,
                                   PdfFont font, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph("TỔNG KẾT")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(30)
                .setMarginBottom(10);
        document.add(sectionTitle);

        Paragraph summary = new Paragraph()
                .setFont(font)
                .setFontSize(11)
                .add("Trong khoảng thời gian từ " + report.getStartDate().format(DATE_FORMATTER) +
                     " đến " + report.getEndDate().format(DATE_FORMATTER) + ", ")
                .add("tổng doanh thu đạt " + formatCurrency(report.getTotalRevenue()) + " ")
                .add("với " + report.getTotalOrders() + " đơn hàng đã giao thành công. ")
                .add("Trong đó, thanh toán COD chiếm " + formatCurrency(report.getTotalCodAmount()) + " ")
                .add("và thanh toán online chiếm " + formatCurrency(report.getTotalOnlineAmount()) + ".")
                .setMarginBottom(20);
        document.add(summary);
    }

    private void addTableHeader(Table table, String text, PdfFont font) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(5)
                .setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell(cell);
    }

    private void addTableRow(Table table, PdfFont font, String... values) {
//        for (String value : values) {
//            Cell cell = new Cell()
//                    .add(new Paragraph(value != null ? value : "").setFontSize(9))
//                    .setPadding(5)
//                    .setTextAlignment(TextAlignment.LEFT);
//            table.addCell(cell);
//        }

        for (String value : values) {
            Cell cell = new Cell()
                    .add(new Paragraph(value != null ? value : "")
                            .setFont(font)  // <-- quan trọng
                            .setFontSize(9))
                    .setPadding(5)
                    .setTextAlignment(TextAlignment.LEFT);
            table.addCell(cell);
        }
    }


    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        return CURRENCY_FORMAT.format(amount) + " VNĐ";
    }
}

