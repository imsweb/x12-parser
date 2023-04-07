package com.lumatax.efile.x12;

import com.imsweb.x12.Element;
import com.imsweb.x12.Loop;
import com.imsweb.x12.Segment;
import com.imsweb.x12.reader.X12Reader.FileType;
import com.imsweb.x12.writer.X12Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mstickel
 */
public class X12TaxFiler {

    public String fileTaxReturn(TaxReturn taxReturn) {
        List<Loop> rootLoops = new ArrayList<>();
        rootLoops.add(buildISALoop(taxReturn));
        X12Writer writer = new X12Writer(FileType.ANSI813_4010, rootLoops);
        String x12String = writer.toX12String();
        return x12String;
    }

    private Loop buildISALoop(TaxReturn taxReturn) {
        Loop loop = new Loop("ISA_LOOP");
        loop.addSegment(buildISASegment(taxReturn.getGenerationDateTime()));
        loop.addSegment(buildIEASegment());
        loop.addLoop(0, buildGSLoop(taxReturn));
        return loop;
    }

    private Segment buildISASegment(LocalDateTime generationDateTime) {
        Segment isaSegment = new Segment("ISA");
        isaSegment.addElement(new Element("ISA01", "03"));
        isaSegment.addElement(new Element("ISA02", "TX813040RP"));
        isaSegment.addElement(new Element("ISA03", "00"));
        isaSegment.addElement(new Element("ISA04", "          "));
        isaSegment.addElement(new Element("ISA05", "ZZ"));
        isaSegment.addElement(new Element("ISA06", "17400000000    "));
        isaSegment.addElement(new Element("ISA07", "ZZ"));
        isaSegment.addElement(new Element("ISA08", "TEX COMPTROLLER"));
        isaSegment.addElement(new Element("ISA09", generationDateTime.format(DateTimeFormatter.ofPattern("YYMMdd"))));
        isaSegment.addElement(new Element("ISA10", generationDateTime.format(DateTimeFormatter.ofPattern("HHmm"))));
        isaSegment.addElement(new Element("ISA11", "U"));
        isaSegment.addElement(new Element("ISA12", "00401"));
        isaSegment.addElement(new Element("ISA13", "000000093"));
        isaSegment.addElement(new Element("ISA14", "0"));
        isaSegment.addElement(new Element("ISA15", "P"));
        isaSegment.addElement(new Element("ISA16", "^"));
        return isaSegment;
    }

    private Loop buildGSLoop(TaxReturn taxReturn) {
        Loop loop = new Loop("GS_LOOP");
        loop.addSegment(buildGSSegment());
        loop.addSegment(buildGESegment());
        loop.addLoop(0, buildSTLoop(taxReturn));
        return loop;
    }

    private Loop buildSTLoop(TaxReturn taxReturn) {
        Loop loop = new Loop("ST_LOOP");
        loop.addSegment(0, buildSTSegment());
        loop.addSegment(1, buildSESegment());
        loop.addLoop(0, buildTILoop(taxReturn));
        return loop;
    }

    private Loop buildTILoop(TaxReturn taxReturn) {
        Loop loop = new Loop("TI_LOOP");
        loop.addSegment(buildBTISegment());
        loop.addSegment(buildDTMSegment());
        loop.addSegment(build4088TIASegment(taxReturn));
        loop.addSegment(build4240TIASegment(taxReturn));
        loop.addSegment(build4006TIASegment(taxReturn));
        buildContactSegments(taxReturn).stream().forEach(s -> loop.addSegment(s));
        loop.addLoop(0, buildTFLoop(taxReturn));
        return loop;
    }

    private Loop buildTFLoop(TaxReturn taxReturn) {
        Loop loop = new Loop("TF_LOOP");
        // only handle initial filings for now
        loop.addSegment(build26100TFSSegment(taxReturn));
        List<Loop> loops = build26100FGLoops(taxReturn);
        for (int c = 0; c < loops.size(); c++) {
            loop.addLoop(c, loops.get(c));
        }
        return loop;
    }

    private List<Loop> build26100FGLoops(TaxReturn taxReturn) {
        return taxReturn.getPhysicalLocations().stream()
                        .filter(pl -> !pl.getState().equals(taxReturn.getStateFilingIn())) // out of state locations only
                        .map(pl -> {
                            Loop loop = new Loop("FG_LOOP");
                            Segment segment = new Segment("FGS");
                            segment.addElement("FGS01", "LOC");
                            segment.addElement("FGS02", "LU");
                            segment.addElement("FGS03", pl.getOutletNumber());
                            loop.addSegment(segment);
                            loop.addSegment(build4011TIASegment(taxReturn));
                            loop.addSegment(build4012TIASegment(taxReturn));
                            loop.addSegment(build4013TIASegment(taxReturn));
                            loop.addSegment(build4014TIASegment(taxReturn));
                            return loop;
                        }).collect(Collectors.toList());
    }

    private Segment build4011TIASegment(TaxReturn taxReturn) {
        Segment segment = new Segment("TIA");
        segment.addElement("TIA01", "4011");
        segment.addElement("TIA02", taxReturn.getTotalSales().toPlainString());
        return segment;
    }

    private Segment build4012TIASegment(TaxReturn taxReturn) {
        Segment segment = new Segment("TIA");
        segment.addElement("TIA01", "4012");
        segment.addElement("TIA02", taxReturn.getTaxableSales().toPlainString());
        return segment;
    }

    private Segment build4013TIASegment(TaxReturn taxReturn) {
        Segment segment = new Segment("TIA");
        segment.addElement("TIA01", "4013");
        segment.addElement("TIA02", taxReturn.getTaxablePurchases().toPlainString());
        return segment;
    }

    private Segment build4014TIASegment(TaxReturn taxReturn) {
        Segment segment = new Segment("TIA");
        segment.addElement("TIA01", "4014");
        segment.addElement("TIA02", taxReturn.getAmountSubjectToStateTax().toPlainString());
        segment.addElement("TIA03", "");
        segment.addElement("TIA04", "");
        segment.addElement("TIA05", "");
        segment.addElement("TIA06", taxReturn.getStateSalesTaxRate().toPlainString());
        return segment;
    }

    private Segment build26100TFSSegment(TaxReturn taxReturn) {
        Segment segment = new Segment("TFS");
        segment.addElement("TFS01", "T2");
        segment.addElement("TFS02", "26100");
        return segment;
    }

    private List<Segment> buildContactSegments(TaxReturn taxReturn) {
        List<Segment> segments = new ArrayList<>();
        Segment nameSegment = new Segment("N1");
        nameSegment.addElement("N101", "TP");
        Segment addressSegment = new Segment("N3");
        addressSegment.addElement("N301", taxReturn.getContact().getAddressLine1());
        Segment cityStateZipSegment = new Segment("N4");
        cityStateZipSegment.addElement("N401", taxReturn.getContact().getCity());
        Segment contactPersonSegment = new Segment("PER");
        contactPersonSegment.addElement("PER01", "CN");
        segments.add(nameSegment);
        segments.add(addressSegment);
        segments.add(cityStateZipSegment);
        segments.add(contactPersonSegment);
        return segments;
    }

    private Segment build4240TIASegment(TaxReturn taxReturn) {
        Segment segment = new Segment("TIA");
        segment.addElement("TIA01", "4240");
        segment.addElement("TIA02", "");
        segment.addElement("TIA03",
                           taxReturn.isTaxWasRefundedForItemsExportedOutsideTheUSBasedOnExportCertificate() ? "Y"
                                                                                                            : "N");
        return segment;
    }

    private Segment build4088TIASegment(TaxReturn taxReturn) {
        Segment segment = new Segment("TIA");
        segment.addElement("TIA01", "4088");
        segment.addElement("TIA02", "");
        segment.addElement("TIA03", taxReturn.isTakingCreditToReduceTaxesDue() ? "Y" : "N");
        segment.addElement("TIA04", taxReturn.isTakingCreditForTaxesRemitted() ? "Y" : "N");
        segment.addElement("TIA05",
                           taxReturn.isTakingBadCreditForTaxesRemittedAndAssignedByAnotherEntity() ? "Y" : "N");
        return segment;
    }

    private Segment build4006TIASegment(TaxReturn taxReturn) {
        Segment segment = new Segment("TIA");
        segment.addElement("TIA01", "4006");
        segment.addElement("TIA02", "");
        segment.addElement("TIA03", taxReturn.isAddressChanged() ? "Y" : "N");
        return segment;
    }

    private Segment buildDTMSegment() {
        Segment segment = new Segment("DTM");
        segment.addElement("DTM01", "683");
        return segment;
    }

    private Segment buildBTISegment() {
        Segment segment = new Segment("BTI");
        segment.addElement("BTI01", "T6");
        return segment;
    }

    private Segment buildIEASegment() {
        Segment ieaSegment = new Segment("IEA");
        ieaSegment.addElement("IEA01", "3");
        ieaSegment.addElement("IEA02", "123456789");
        return ieaSegment;
    }

    private Segment buildGSSegment() {
        Segment gsSegment = new Segment("GS");
        gsSegment.addElement("GS01", "TF");
        return gsSegment;
    }

    private Segment buildSTSegment() {
        Segment stSegment = new Segment("ST");
        stSegment.addElement("ST01", "813");
        return stSegment;
    }

    private Segment buildGESegment() {
        Segment segment = new Segment("GE");
        segment.addElement("GE01", "1");
        return segment;
    }

    private Segment buildSESegment() {
        Segment segment = new Segment("SE");
        segment.addElement("SE01", "74");
        return segment;
    }
}
