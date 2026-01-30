package com.company.claims.model;

import java.util.ArrayList;
import java.util.List;

public class ClaimValidator {

    private static final String[] MANDATORY_FIELDS = {
            "policyNumber",
            "policyholderName",
            "incidentDate",
            "incidentTime",
            "incidentLocation",
            "incidentDescription",
            "claimantName",
            "claimantContactDetails",
            "assetType",
            "assetId",
            "estimatedDamage",
            "claimType",
            "initialEstimate"
    };

    public List<String> findMissingFields(ExtractedFields d) {
        List<String> missing = new ArrayList<>();
        for (String field : MANDATORY_FIELDS) {
            if (isMissing(field, d)) {
                missing.add(field);
            }
        }
        return missing;
    }

    private boolean isMissing(String field, ExtractedFields d) {
        if (d == null) return true;
        switch (field) {
            case "policyNumber":
                return d.policyInformation == null ||
                        isEmpty(d.policyInformation.policyNumber);

            case "policyholderName":
                return d.policyInformation == null ||
                        isEmpty(d.policyInformation.policyholderName);

            case "incidentDate":
                return d.incidentInformation == null ||
                        isEmpty(d.incidentInformation.date);

            case "incidentTime":
                return d.incidentInformation == null ||
                        isEmpty(d.incidentInformation.time);

            case "incidentLocation":
                return d.incidentInformation == null ||
                        isEmpty(d.incidentInformation.location);

            case "incidentDescription":
                return d.incidentInformation == null ||
                        isEmpty(d.incidentInformation.description);

            case "claimantName":
                return d.involvedParties == null ||
                        d.involvedParties.claimant == null ||
                        isEmpty(d.involvedParties.claimant.name);

            case "claimantContactDetails":
                return d.involvedParties == null ||
                        d.involvedParties.claimant == null ||
                        d.involvedParties.claimant.contactDetails == null ||
                        (isEmpty(d.involvedParties.claimant.contactDetails.primaryPhone) &&
                                isEmpty(d.involvedParties.claimant.contactDetails.primaryEmail));

            case "assetType":
                return d.assetDetails == null ||
                        isEmpty(d.assetDetails.assetType);

            case "assetId":
                return d.assetDetails == null ||
                        isEmpty(d.assetDetails.assetId);

            case "estimatedDamage":
                return d.assetDetails == null ||
                        isEmpty(d.assetDetails.estimatedDamage);

            case "claimType":
                return d.otherMandatoryFields == null ||
                        isEmpty(d.otherMandatoryFields.claimType);

            case "initialEstimate":
                return d.otherMandatoryFields == null ||
                        isEmpty(d.otherMandatoryFields.initialEstimate);

            default:
                return false;
        }
    }

    private boolean isEmpty(String v) {
        return v == null || v.trim().isEmpty();
    }
}