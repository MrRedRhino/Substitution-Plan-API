package org.pipeman.sp_api.pdfs;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.utilities.PdfTable;
import com.spire.pdf.utilities.PdfTableExtractor;

import java.awt.geom.Rectangle2D;

@JsonSerialize(using = PdfDataSerializer.class)
public class PlanData {
    private final String message;
    private final Row[] substitutions;

    private PlanData(String message, Row[] substitutions) {
        this.message = message;
        this.substitutions = substitutions;
    }

    public static PlanData from(PdfDocument pdf) {
        PdfTable table = new PdfTableExtractor(pdf).extractTable(0)[0];

        Row[] substitutions = new Row[table.getRowCount() - 1];
        if (substitutions.length > 1) {
            for (int i = 1; i <= substitutions.length; i++) {
                substitutions[i - 1] = Row.extractFromTable(i, table);
            }
        }

        String[] lines = pdf.getPages().get(0).extractText(new Rectangle2D.Float(0, 140, 1000, 1000)).split("\n");
        StringBuilder output = new StringBuilder();
        if (lines.length > 2)
            for (int i = 2; i < lines.length; i++) {
                String line = lines[i].strip();
                if (line.isEmpty()) break;
                if (i > 2) output.append('\n');
                output.append(line);
            }
        return new PlanData(output.toString(), substitutions);
    }

    public Row[] substitutions() {
        return substitutions;
    }

    public String message() {
        return message;
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
