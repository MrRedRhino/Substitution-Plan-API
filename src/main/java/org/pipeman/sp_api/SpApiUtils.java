package org.pipeman.sp_api;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class SpApiUtils {
    public static String convertPdfToHtml(byte[] input) {
        OutputStream output = new ByteArrayOutputStream();
        new PdfDocument(input).saveToStream(output, FileFormat.HTML);
        return output.toString()
                .replace("Evaluation Warning : The document was created with Spire.PDF for java.", "");
    }
}
