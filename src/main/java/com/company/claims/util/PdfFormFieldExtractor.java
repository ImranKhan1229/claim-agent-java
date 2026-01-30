package com.company.claims.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PdfFormFieldExtractor {

    public static Map<String, String> extract(String pdfPath) throws Exception {
        Map<String, String> fieldValues = new HashMap<>();

        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDAcroForm pdAcroForm = document.getDocumentCatalog().getAcroForm();

            if (pdAcroForm == null) {
                return fieldValues;
            }

            for (PDField field : pdAcroForm.getFieldTree()) {
                fieldValues.put(
                        field.getFullyQualifiedName(),
                        field.getValueAsString()
                );
            }
        }
        return fieldValues;
    }
}
