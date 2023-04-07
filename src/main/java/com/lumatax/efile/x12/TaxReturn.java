package com.lumatax.efile.x12;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author mstickel
 */
public class TaxReturn {

    // report metadata
    private LocalDateTime generationDateTime;
    private String stateFilingIn;
    // credit
    private boolean takingCreditToReduceTaxesDue;
    private boolean takingCreditForTaxesRemitted;
    private boolean wasCreditReportedOnFederalIncomeTaxReport;
    private boolean takingBadCreditForTaxesRemittedAndAssignedByAnotherEntity;
    private boolean tookCreditOnFederalReturnForOneOrMoreThirdPartyEntities;
    // export
    private boolean taxWasRefundedForItemsExportedOutsideTheUSBasedOnExportCertificate;
    // address change
    private boolean addressChanged;
    // contact
    private Address contact;
    // physical locations
    private List<PhysicalLocation> physicalLocations;
    // tax data
    private BigDecimal totalSales;
    private BigDecimal taxableSales;
    private BigDecimal taxablePurchases;
    private BigDecimal amountSubjectToStateTax;
    private BigDecimal stateSalesTaxRate;

    public LocalDateTime getGenerationDateTime() {
        return generationDateTime;
    }

    public void setGenerationDateTime(LocalDateTime generationDateTime) {
        this.generationDateTime = generationDateTime;
    }

    public String getStateFilingIn() {
        return stateFilingIn;
    }

    public void setStateFilingIn(String stateFilingIn) {
        this.stateFilingIn = stateFilingIn;
    }

    public boolean isTakingCreditToReduceTaxesDue() {
        return takingCreditToReduceTaxesDue;
    }

    public void setTakingCreditToReduceTaxesDue(boolean takingCreditToReduceTaxesDue) {
        this.takingCreditToReduceTaxesDue = takingCreditToReduceTaxesDue;
    }

    public boolean isTakingCreditForTaxesRemitted() {
        return takingCreditForTaxesRemitted;
    }

    public void setTakingCreditForTaxesRemitted(boolean takingCreditForTaxesRemitted) {
        this.takingCreditForTaxesRemitted = takingCreditForTaxesRemitted;
    }

    public boolean isWasCreditReportedOnFederalIncomeTaxReport() {
        return wasCreditReportedOnFederalIncomeTaxReport;
    }

    public void setWasCreditReportedOnFederalIncomeTaxReport(boolean wasCreditReportedOnFederalIncomeTaxReport) {
        this.wasCreditReportedOnFederalIncomeTaxReport = wasCreditReportedOnFederalIncomeTaxReport;
    }

    public boolean isTakingBadCreditForTaxesRemittedAndAssignedByAnotherEntity() {
        return takingBadCreditForTaxesRemittedAndAssignedByAnotherEntity;
    }

    public void setTakingBadCreditForTaxesRemittedAndAssignedByAnotherEntity(boolean takingBadCreditForTaxesRemittedAndAssignedByAnotherEntity) {
        this.takingBadCreditForTaxesRemittedAndAssignedByAnotherEntity
                = takingBadCreditForTaxesRemittedAndAssignedByAnotherEntity;
    }

    public boolean isTookCreditOnFederalReturnForOneOrMoreThirdPartyEntities() {
        return tookCreditOnFederalReturnForOneOrMoreThirdPartyEntities;
    }

    public void setTookCreditOnFederalReturnForOneOrMoreThirdPartyEntities(boolean tookCreditOnFederalReturnForOneOrMoreThirdPartyEntities) {
        this.tookCreditOnFederalReturnForOneOrMoreThirdPartyEntities
                = tookCreditOnFederalReturnForOneOrMoreThirdPartyEntities;
    }

    public boolean isTaxWasRefundedForItemsExportedOutsideTheUSBasedOnExportCertificate() {
        return taxWasRefundedForItemsExportedOutsideTheUSBasedOnExportCertificate;
    }

    public void setTaxWasRefundedForItemsExportedOutsideTheUSBasedOnExportCertificate(boolean taxWasRefundedForItemsExportedOutsideTheUSBasedOnExportCertificate) {
        this.taxWasRefundedForItemsExportedOutsideTheUSBasedOnExportCertificate
                = taxWasRefundedForItemsExportedOutsideTheUSBasedOnExportCertificate;
    }

    public boolean isAddressChanged() {
        return addressChanged;
    }

    public void setAddressChanged(boolean addressChanged) {
        this.addressChanged = addressChanged;
    }

    public Address getContact() {
        return contact;
    }

    public void setContact(Address contact) {
        this.contact = contact;
    }

    public List<PhysicalLocation> getPhysicalLocations() {
        return physicalLocations;
    }

    public void setPhysicalLocations(List<PhysicalLocation> physicalLocations) {
        this.physicalLocations = physicalLocations;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getTaxableSales() {
        return taxableSales;
    }

    public void setTaxableSales(BigDecimal taxableSales) {
        this.taxableSales = taxableSales;
    }

    public BigDecimal getTaxablePurchases() {
        return taxablePurchases;
    }

    public void setTaxablePurchases(BigDecimal taxablePurchases) {
        this.taxablePurchases = taxablePurchases;
    }

    public BigDecimal getAmountSubjectToStateTax() {
        return amountSubjectToStateTax;
    }

    public void setAmountSubjectToStateTax(BigDecimal amountSubjectToStateTax) {
        this.amountSubjectToStateTax = amountSubjectToStateTax;
    }

    public BigDecimal getStateSalesTaxRate() {
        return stateSalesTaxRate;
    }

    public void setStateSalesTaxRate(BigDecimal stateSalesTaxRate) {
        this.stateSalesTaxRate = stateSalesTaxRate;
    }
}
