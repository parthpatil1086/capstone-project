package com.example.capstone_swastik;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class ProcurementDetailsActivity extends AppCompatActivity {

    private static final int CREATE_FILE_REQUEST_CODE = 1001;

    TextView supplier, billNumberTextView, date, qty, price, total, status, payment;
    Button btnGeneratePDF;

    // Store current procurement data for PDF generation
    String supplierName, billNumber, dateStr, quantity, pricePerUnit, totalAmount, statusStr, paymentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procurement_details);

        supplier = findViewById(R.id.detailSupplierName);
        billNumberTextView = findViewById(R.id.detailBillNumber);
        date = findViewById(R.id.detailDate);
        qty = findViewById(R.id.detailQty);
        price = findViewById(R.id.detailPrice);
        total = findViewById(R.id.detailTotal);
        status = findViewById(R.id.detailStatus);
        payment = findViewById(R.id.detailPaymentDate);
        btnGeneratePDF = findViewById(R.id.btnGeneratePDF);

        // Get data from Intent
        supplierName = getIntent().getStringExtra("supplierName");
        billNumber = getIntent().getStringExtra("billNumber");
        dateStr = getIntent().getStringExtra("date");
        quantity = getIntent().getStringExtra("quantity");
        pricePerUnit = getIntent().getStringExtra("pricePerUnit");
        totalAmount = getIntent().getStringExtra("totalAmount");
        statusStr = getIntent().getStringExtra("status");
        paymentDate = getIntent().getStringExtra("paymentDate");

        // Set values
        supplier.setText(supplierName);
        billNumberTextView.setText(billNumber != null ? "Bill Number: " + billNumber : "Bill Number: N/A");
        date.setText(dateStr != null ? dateStr : "N/A");
        qty.setText(quantity != null ? quantity : "0");
        price.setText(pricePerUnit != null ? pricePerUnit : "0");
        total.setText(totalAmount != null ? totalAmount : "0");
        payment.setText(paymentDate != null ? paymentDate : "N/A");

        // Dynamic status color
        if (statusStr != null) {
            status.setText(statusStr);
            switch (statusStr.toLowerCase()) {
                case "paid":
                    status.setTextColor(Color.parseColor("#388E3C"));
                    break;
                case "pending":
                    status.setTextColor(Color.parseColor("#F57C00"));
                    break;
                case "failed":
                    status.setTextColor(Color.parseColor("#D32F2F"));
                    break;
                default:
                    status.setTextColor(Color.BLACK);
            }
        }

        // PDF button click -> open system file picker
        btnGeneratePDF.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "Bill_" + (billNumber != null ? billNumber : System.currentTimeMillis()) + ".pdf");
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                generatePDF(uri);
            }
        }
    }

    private void generatePDF(Uri uri) {
        Document document = new Document();
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.BLACK);
            Font subTitleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.DARK_GRAY);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
            Font contentFont = new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL, BaseColor.BLACK);

            // Logo
            try {
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.shree_swastik_default);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Image logo = Image.getInstance(stream.toByteArray());
                logo.scaleToFit(100, 100);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Factory name
            Paragraph factoryName = new Paragraph("Shreesvastik Agro Sakharale", titleFont);
            factoryName.setAlignment(Element.ALIGN_CENTER);
            document.add(factoryName);

            // Subtitle
            Paragraph billTitle = new Paragraph("Bill Receipt of Sugarcane", subTitleFont);
            billTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(billTitle);

            document.add(new Paragraph(" ")); // spacing

            // Bill Number separate above table
            Paragraph billNumPara = new Paragraph("Bill Number: " + (billNumber != null ? billNumber : "N/A"), headerFont);
            billNumPara.setAlignment(Element.ALIGN_LEFT);
            document.add(billNumPara);

            document.add(new Paragraph(" ")); // spacing

            // Table for procurement details
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(15f);
            table.setSpacingAfter(10f);

            addCell(table, "Supplier Name", headerFont);
            addCell(table, supplierName, contentFont);

            addCell(table, "Date", headerFont);
            addCell(table, dateStr != null ? dateStr : "N/A", contentFont);

            addCell(table, "Quantity", headerFont);
            addCell(table, quantity != null ? quantity : "0", contentFont);

            addCell(table, "Price/Unit", headerFont);
            addCell(table, pricePerUnit != null ? pricePerUnit : "0", contentFont);

            addCell(table, "Total Amount", headerFont);
            addCell(table, totalAmount != null ? totalAmount : "0", contentFont);

            addCell(table, "Status", headerFont);
            BaseColor statusColor;
            switch (statusStr != null ? statusStr.toLowerCase() : "") {
                case "paid": statusColor = new BaseColor(56, 142, 60); break;
                case "pending": statusColor = new BaseColor(245, 124, 0); break;
                case "failed": statusColor = new BaseColor(211, 47, 47); break;
                default: statusColor = BaseColor.LIGHT_GRAY;
            }
            PdfPCell statusCell = new PdfPCell(new Paragraph(statusStr != null ? statusStr : "Pending", contentFont));
            statusCell.setBackgroundColor(statusColor);
            statusCell.setPadding(10f);
            table.addCell(statusCell);

            addCell(table, "Payment Date", headerFont);
            addCell(table, paymentDate != null ? paymentDate : "N/A", contentFont);

            document.add(table);
            document.close();

            Toast.makeText(this, "PDF Saved Successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setPadding(5f);
        table.addCell(cell);
    }
}
