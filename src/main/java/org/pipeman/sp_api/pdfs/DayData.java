package org.pipeman.sp_api.pdfs;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import org.pipeman.sp_api.LazyInitializer;
import org.pipeman.sp_api.Main;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DayData {
    private final long creationTime;
    private final LazyInitializer<String> html;
    private final LazyInitializer<PlanData> data;
    private final LazyInitializer<byte[]> image;
    private final byte[] pdf;

    public DayData(byte[] data) {
        this.creationTime = System.currentTimeMillis();
        this.pdf = data;
        PdfDocument document = new PdfDocument(data);
        this.html = new LazyInitializer<>(() -> convertToHtml(document));
        this.data = new LazyInitializer<>(() -> PlanData.from(document));
        this.image = new LazyInitializer<>(() -> convertToImage(document));
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
}
