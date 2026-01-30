package com.company.claims.parser;

import com.company.claims.model.*;

import java.util.Map;

public class FNOLParser {

    public ExtractedFields parse(Map<String, String> f) {

        ExtractedFields e = new ExtractedFields();

        // Policy
        PolicyInformation p = new PolicyInformation();
        p.policyNumber = f.getOrDefault("Text7", "");
        p.policyholderName =
                f.getOrDefault("NAME OF INSURED First Middle Last", "");
        p.effectiveDates = f.getOrDefault("Text1", "");
        e.policyInformation = p;

        // Incident
        IncidentInformation i = new IncidentInformation();
        i.date = f.getOrDefault("Text3", "");
        String hour = f.getOrDefault("Text4", "").trim();
        String amPm = "";
        if ("Yes".equalsIgnoreCase(f.get("Check Box5"))) {
            amPm = "AM";
        } else if ("Yes".equalsIgnoreCase(f.get("Check Box6"))) {
            amPm = "PM";
        }
        i.time = hour.isEmpty()
                ? ""
                : hour + (amPm.isEmpty() ? "" : " " + amPm);
        i.description = f.getOrDefault(
                "DESCRIPTION OF ACCIDENT ACORD 101 Additional Remarks Schedule may be attached if more space is required",
                ""
        );
        StringBuilder location = new StringBuilder();
        append(location, f.get("STREET LOCATION OF LOSS"));
        append(location, f.get("CITY STATE ZIP"));
        append(location, f.get("COUNTRY"));
        append(location, f.get("DESCRIBE LOCATION OF LOSS IF NOT AT SPECIFIC STREET ADDRESS"));
        i.location = location.toString();
        e.incidentInformation = i;

        // Involved Parties
        InvolvedParties ip = new InvolvedParties();
        Claimant c = new Claimant();
        c.name = f.getOrDefault("NAME OF INSURED First Middle Last", "");
        ContactDetails cd = new ContactDetails();
        cd.primaryPhone = f.getOrDefault("PHONE  CELL HOME BUS PRIMARY", "");
        cd.secondaryPhone = f.getOrDefault("PHONE  SECONDARY CELL HOME BUS", "");
        cd.primaryEmail = f.getOrDefault("PRIMARY EMAIL ADDRESS", "");
        cd.secondaryEmail = f.getOrDefault("SECONDARY EMAIL ADDRESS", "");
        c.contactDetails = cd;
        ip.claimant = c;
        e.involvedParties = ip;

        ThirdParties tp = new ThirdParties();
        tp.injured =
                hasAny(
                        f.get("NAME  ADDRESSRow1"),
                        f.get("PHONE AC NoRow1"),
                        f.get("Text83"),

                        f.get("NAME  ADDRESSRow2"),
                        f.get("PHONE AC NoRow2"),
                        f.get("Text86"),

                        f.get("NAME  ADDRESSRow3"),
                        f.get("PHONE AC NoRow3"),
                        f.get("Text87"),

                        f.get("NAME  ADDRESSRow4"),
                        f.get("PHONE AC NoRow4"),
                        f.get("Text89")
                );

        tp.otherVehicleOrPropertyDamaged =
                hasAny(
                        f.get("NON  VEHICLE"),
                        f.get("TYPE BODY_2"),
                        f.get("MODEL"),
                        f.get("VIN"),
                        f.get("DESCRIBE PROPERTY Other Than Vehicle")
                );

        tp.witnessesOrPassengers =
                hasAny(
                        f.get("NAME  ADDRESSRow1_2"),
                        f.get("PHONE AC NoRow1_2"),

                        f.get("NAME  ADDRESSRow2_2"),
                        f.get("PHONE AC NoRow2_2"),

                        f.get("Text93"),
                        f.get("NAME  ADDRESSRow3_2"),
                        f.get("PHONE AC NoRow3_2")
                );

        tp.present = tp.injured || tp.otherVehicleOrPropertyDamaged || tp.witnessesOrPassengers;
        ip.thirdParties = tp;

        // Asset
        AssetDetails a = new AssetDetails();
        a.assetType = f.getOrDefault("TYPE BODY_2", "");
        a.assetId = f.getOrDefault("VIN", "");
        a.estimatedDamage = f.getOrDefault("ESTIMATE AMOUNT_2", "");
        e.assetDetails = a;

        // Other Mandatory
        OtherMandatoryFields o = new OtherMandatoryFields();
        o.claimType = tp.injured ? "injury" : "vehicle";
        String description = f.getOrDefault(
                "DESCRIPTION OF ACCIDENT ACORD 101 Additional Remarks Schedule may be attached if more space is required",
                ""
        );
        String remarks = f.getOrDefault(
                "REMARKS ACORD 101 Additional Remarks Schedule may be attached if more space is required",
                ""
        );
        o.attachments = description.length() > 200 || remarks.length() > 200
                        || containsIgnoreCase(description, "ACORD 101")
                        || containsIgnoreCase(remarks, "ACORD 101");

        o.initialEstimate = f.getOrDefault("Text45", "");
        e.otherMandatoryFields = o;

        return e;
    }

    private void append(StringBuilder sb, String value) {
        if (value != null && !value.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(value.trim());
        }
    }

    private boolean hasAny(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean containsIgnoreCase(String value, String token) {
        return value != null && token != null &&
                value.toLowerCase().contains(token.toLowerCase());
    }
}
