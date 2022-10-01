package org.pipeman.sp_api;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class SpApiUtils {
    public static String convertPdfToHtml(byte[] input) {
        PdfDocument pdf = new PdfDocument(input);

        OutputStream output = new ByteArrayOutputStream();
        pdf.saveToStream(output, FileFormat.HTML);
        return output.toString();
    }
}
