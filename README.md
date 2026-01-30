# Autonomous Insurance Claims Processing Agent (Java)

This project extracts key First Notice of Loss (FNOL) information from a **fillable ACORD – Automobile Loss Notice PDF**, validates mandatory fields, and applies routing rules to determine how a claim should be processed.

The output is a structured JSON containing:
- extracted fields
- missing mandatory fields (if any)
- recommended routing decision
- reasoning for the decision

---

## 1. Output JSON Structure

```json
{
  "extractedFields": {},
  "missingFields": [],
  "recommendedRoute": "",
  "reasoning": ""
}
````

---

## 2. Fields Extracted

### 2.1 Policy Information

Extracted from Page 1 – Insured section.

* `policyNumber` -> POLICY NUMBER
* `policyholderName` -> NAME OF INSURED (First, Middle, Last)
* `effectiveDates` → DATE (MM/DD/YYYY)

---

### 2.2 Incident Information

Extracted from Page 1 – Loss section.

* `date` → DATE OF LOSS
* `time` → DATE OF LOSS AND TIME (AM / PM)
* `location` → LOCATION OF LOSS (combined address fields)
* `description` → DESCRIPTION OF ACCIDENT

---

### 2.3 Involved Parties

#### Claimant

Extracted from Page 1 – Insured section.

* `name`
* `contactDetails`

    * primaryPhone
    * secondaryPhone
    * primaryEmail
    * secondaryEmail

**Validation rule:**
At least primary phone & primary email must be present.

#### Third Parties (Flags Only)

```json
"thirdParties": {
  "present": false,
  "injured": false,
  "otherVehicleOrPropertyDamaged": false,
  "witnessesOrPassengers": false
}
```

**Logic:**

* `injured = true` → INJURED section has data
* `otherVehicleOrPropertyDamaged = true` → OTHER VEHICLE / PROPERTY DAMAGED has data
* `witnessesOrPassengers = true` → WITNESSES OR PASSENGERS has data
* `present` is **derived** based on the values of above field. Please refer below logic to understand more:

```java
present = injured || otherVehicleOrPropertyDamaged || witnessesOrPassengers; 
```

**Important:**
If `present = false`, which mean other Vehicle or Property is not damaged, No one is injured and witness the incident.

---

### 2.4 Asset Details (Insured Vehicle)

Extracted from Page 1 – Insured Vehicle section.

* `assetType` → TYPE / BODY
* `assetId` → V.I.N.
* `estimatedDamage` → ESTIMATE AMOUNT (insured vehicle only)

**Clarification:**
ACORD also contains an estimate for third-party damage.
For routing, **only the insured vehicle estimate is used**.

---

### 2.5 Other Mandatory Fields

```json
"otherMandatoryFields": {
  "claimType": "",
  "attachments": false,
  "initialEstimate": ""
}
```

* `claimType`

    * `"injury"` if any injured party exists
    * `"vehicle"` otherwise
* `attachments`

    * Boolean flag indicating presence of ACORD 101 (Additional Remarks Schedule). If description or remark content length is more then 200 chars then attachment is true. As we cannot attach additional page to given PDF.
    * Defaulted to `false`

```java
attachments = description.length > 200 || remarks.length > 200 
```

* `initialEstimate`

    * Same value as insured vehicle estimated damage

---

## 3. Mandatory Fields Validation

The following fields are treated as **business-mandatory**:

* policyNumber
* policyholderName
* incidentDate
* incidentTime
* incidentLocation
* incidentDescription
* claimantName
* claimantContactDetails
* assetType
* assetId
* estimatedDamage
* claimType
* initialEstimate

**Not treated as missing:**

* attachments (boolean)
* third parties when `present = false`

If **any mandatory field is missing**, the claim is routed to **Manual Review**.

---

## 4. Routing Rules (Priority-Based)

Routing is evaluated in the following order:

1. Missing mandatory fields → **Manual Review**
2. Description contains keywords
   (`fraud`, `inconsistent`, `staged`) → **Investigation Flag**
3. Claim type = `injury` → **Specialist Queue**
4. Insured estimated damage < 25,000 → **Fast-track**
5. Default → **Manual Review**

Only **one final route** is returned.

---

## 5. Assumptions

1. Input PDF is a **fillable ACORD (AcroForm)**.
2. Effective dates not present in ACORD Pdf. Assuming DATE (MM/DD/YYYY) at top right as effective date.
3. Pdf structure should remains the same. Any changes in the structure of the PDF can affect business logic.
4. Fast-track routing is based on **insured vehicle damage only**.
5. In Involved Parties: I am assuming Contact Details is of Claimant.
6. Attachments means if DESCRIPTION OF ACCIDENT and REMARKS field contains more than 200 chars then attachment is required. 

---

## 6. Risks

* PDF field names may vary (e.g., `Text7`, `Text89`).
* Non-fillable or scanned PDFs will not return form fields.
* Multiple ESTIMATE AMOUNT fields require correct contextual mapping.
* Attachments cannot be detected unless explicitly present i.

---

## 7. Dependencies

* Java 17
* Maven
* Apache PDFBox
* Jackson Databind

---

## 8. Build & Run Instructions

### Build the project

```
mvn clean install
```

### Run the application

```
java -jar .\target\claims-agent-1.0-SNAPSHOT.jar ..\ACORD-Automobile-Loss-Notice-12.05.16.pdf
```

* I have already build the project which you can locate the jar at .\target\claims-agent-1.0-SNAPSHOT.jar path
---

## 9. Project Structure

```
src/
 └── main/
     └── java/
         └── com/company/claims/
             ├── Main.java
             ├── parser/
             │   └── FNOlParser.java
             ├── routing/
             │   └── ClaimRouter.java
             ├── util/
             │   ├── PdfFormFieldExtractor.java
             └── model/
                 ├── ClaimOutput.java
                 ├── ExtractedFields.java
                 ├── PolicyInformation.java
                 ├── IncidentInformation.java
                 ├── InvolvedParties.java
                 ├── Claimant.java
                 ├── ContactDetails.java
                 ├── ThirdParties.java
                 ├── AssetDetails.java
                 ├── OtherMandatoryFields.java
                 ├── ClaimValidator.java
                 └── RouteResult.java
```

---

## 10. Notes
* Validation is business-driven, not form-driven.
* The solution aligns with real FNOL intake system behavior.
