package com.company.claims;

import com.company.claims.model.*;
import com.company.claims.parser.FNOLParser;
import com.company.claims.routing.ClaimRouter;
import com.company.claims.util.PdfFormFieldExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("Usage: java -jar claims-agent.jar <FNOL_PDF_PATH>");
            return;
        }

        Map<String, String> formFields = PdfFormFieldExtractor.extract(args[0]);
        // formFields.forEach((k, v) -> System.out.println(k + " = " + v));

        ExtractedFields extracted =
                new FNOLParser().parse(formFields);

        List<String> missing =
                new ClaimValidator().findMissingFields(extracted);

        RouteResult route =
                new ClaimRouter().route(extracted, missing);

        ClaimOutput output =
                new ClaimOutput(extracted, missing, route);

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(output)
        );
    }
}
