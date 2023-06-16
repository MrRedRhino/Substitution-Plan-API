package org.pipeman.sp_api.pdfs;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import org.pipeman.sp_api.Config;
import org.pipeman.sp_api.LazyInitializer;
import org.pipeman.sp_api.Main;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Function;

public class DayData {
    private final long creationTime;
    private final LazyInitializer<String> html;
    private final LazyInitializer<PlanData> data;
    private final LazyInitializer<byte[]> image;
    private final byte[] pdf;
    private final PdfDocument document;

    public DayData(byte[] data) {
        this.creationTime = System.currentTimeMillis();
        this.pdf = data;
        this.document = new PdfDocument(data);
        this.html = new LazyInitializer<>(() -> runWithPdfDocument(this::convertToHtml));
        this.data = new LazyInitializer<>(() -> runWithPdfDocument(PlanData::from));
        this.image = new LazyInitializer<>(() -> runWithPdfDocument(this::convertToImage));
    }

    public long creationTime() {
        return creationTime;
    }

    public String html() {
        return html.get();
    }

    public PlanData data() {
        return data.get();
    }

    public byte[] image() {
        return image.get();
    }

    public byte[] pdf() {
        return pdf;
    }

    private String convertToHtml(PdfDocument document) {
        OutputStream output = new ByteArrayOutputStream();
        document.saveToStream(output, FileFormat.HTML);
        return output.toString().replace(Main.SPIRE_WARNING, "");
    }

    private byte[] convertToImage(PdfDocument pdf) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(pdf.saveAsImage(0), "png", os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return os.toByteArray();
    }

    private synchronized <T> T runWithPdfDocument(Function<PdfDocument, T> action) {
        return action.apply(document);
    }
}
