package com.lumatax.efile.x12;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author mstickel
 */
public class X12TaxFilerTest {

    @Test
    public void testFile() {
        TaxReturn taxReturn = new TaxReturn();
        taxReturn.setGenerationDateTime(LocalDateTime.now());
        taxReturn.setStateFilingIn("TX");
        taxReturn.setTakingCreditToReduceTaxesDue(true);
        taxReturn.setTaxWasRefundedForItemsExportedOutsideTheUSBasedOnExportCertificate(true);
        Address contact = new Address();
        contact.setAddressLine1("8799 Balboa Ave Suite 240");
        contact.setCity("San Diego");
        taxReturn.setContact(contact);
        List<PhysicalLocation> physicalLocations = new ArrayList<>();
        physicalLocations.add(buildPhysicalLocation("TX", "1"));
        physicalLocations.add(buildPhysicalLocation("CA", "2"));
        physicalLocations.add(buildPhysicalLocation("CA", "3"));
        physicalLocations.add(buildPhysicalLocation("NJ", "4"));
        taxReturn.setPhysicalLocations(physicalLocations);
        taxReturn.setTotalSales(new BigDecimal("6000"));
        taxReturn.setTaxableSales(new BigDecimal("5000"));
        taxReturn.setTaxablePurchases(BigDecimal.ZERO);
        taxReturn.setAmountSubjectToStateTax(new BigDecimal("5000"));
        taxReturn.setStateSalesTaxRate(new BigDecimal("0.04"));
        X12TaxFiler taxFiler = new X12TaxFiler();
        String x12 = taxFiler.fileTaxReturn(taxReturn);
        assertNotNull(x12);
        System.out.println("### X12: ###\n\n" + x12.replaceAll("~", "~\n"));
    }

    private PhysicalLocation buildPhysicalLocation(String state, String id) {
        PhysicalLocation physicalLocation = new PhysicalLocation();
        physicalLocation.setState(state);
        physicalLocation.setOutletNumber(id);
        return physicalLocation;
    }
}
