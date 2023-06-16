package org.pipeman.sp_api.pdfs;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.utilities.PdfTable;
import com.spire.pdf.utilities.PdfTableExtractor;

import java.awt.geom.Rectangle2D;
import java.util.regex.Pattern;

@JsonSerialize(using = PdfDataSerializer.class)
public class PlanData {
    private static final Pattern MESSAGE_END = Pattern.compile("Klasse\\s+Std\\s+Vertretung\\s+Lehrer\\s+Raum\\s+");
    private final String message;
    private final Row[] substitutions;
    private final String date;

    private PlanData(String message, Row[] substitutions, String date) {
        this.message = message;
        this.substitutions = substitutions;
        this.date = date;
    }

    public static PlanData from(PdfDocument pdf) {
        PdfTable[] tables = new PdfTableExtractor(pdf).extractTable(0);
        Row[] substitutions = new Row[0];
        if (tables != null) {
            PdfTable table = tables[0];
            substitutions = new Row[table.getRowCount() - 1];

            if (substitutions.length >= 1) {
                for (int i = 1; i <= substitutions.length; i++) {
                    substitutions[i - 1] = Row.extractFromTable(i, table);
                }
            }
        }

        PdfPageBase page = pdf.getPages().get(0);

        String dateText = page.extractText(new Rectangle2D.Float(0, 90, 1000, 10));
        String date = dateText.substring(dateText.lastIndexOf(", ") + 2).strip();

        String[] lines = page.extractText(new Rectangle2D.Float(0, 140, 1000, 1000)).split("\n");
        StringBuilder output = new StringBuilder();
        if (lines.length > 2)
            for (int i = 2; i < lines.length; i++) {
                String line = lines[i].strip();
                if (MESSAGE_END.matcher(line).find()) break;
                if (i > 2) output.append('\n');
                output.append(line);
            }
        return new PlanData(output.toString(), substitutions, date);
    }

    public Row[] substitutions() {
        return substitutions;
    }

    public String message() {
        return message;
    }

    public String date() {
        return date;
    }

    public record Row(String clazz, String lesson, String substitution, String teacher, String room, String other) {
        public static Row extractFromTable(int row, PdfTable table) {
            int column = 0;
            return new Row(
                    table.getText(row, column++),
                    table.getText(row, column++),
                    table.getText(row, column++),
                    table.getText(row, column++),
                    table.getText(row, column++),
                    table.getText(row, column)
            );
        }
    }
}
