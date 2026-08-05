package org.ashcrest.security;

import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.util.ArrayList;
import java.util.List;

/**
 * DNS Reconnaissance Service powered by dnsjava 3.5.3.
 */
public class DnsScanService {

    public record DnsResult(
            String domain,
            List<String> aRecords,
            List<String> aaaaRecords,
            List<String> mxRecords,
            List<String> nsRecords,
            List<String> txtRecords,
            boolean hasSpf,
            boolean hasDmarc,
            boolean hasDnssec
    ) {}

    public static DnsResult scan(String domainName) {
        String cleanDomain = domainName.toLowerCase().replaceAll("^https?://", "").replaceAll("/.*$", "").trim();
        List<String> aRecords = lookupRecords(cleanDomain, Type.A);
        List<String> aaaaRecords = lookupRecords(cleanDomain, Type.AAAA);
        List<String> mxRecords = lookupRecords(cleanDomain, Type.MX);
        List<String> nsRecords = lookupRecords(cleanDomain, Type.NS);
        List<String> txtRecords = lookupRecords(cleanDomain, Type.TXT);
        List<String> dmarcRecords = lookupRecords("_dmarc." + cleanDomain, Type.TXT);
        List<String> dnskeyRecords = lookupRecords(cleanDomain, Type.DNSKEY);

        boolean hasSpf = txtRecords.stream().anyMatch(r -> r.contains("v=spf1"));
        boolean hasDmarc = !dmarcRecords.isEmpty() || txtRecords.stream().anyMatch(r -> r.contains("v=DMARC1"));
        boolean hasDnssec = !dnskeyRecords.isEmpty();

        return new DnsResult(cleanDomain, aRecords, aaaaRecords, mxRecords, nsRecords, txtRecords, hasSpf, hasDmarc, hasDnssec);
    }

    private static List<String> lookupRecords(String nameStr, int type) {
        List<String> list = new ArrayList<>();
        try {
            Lookup lookup = new Lookup(nameStr, type);
            Record[] records = lookup.run();
            if (records != null) {
                for (Record r : records) {
                    list.add(r.rdataToString());
                }
            }
        } catch (Exception ignored) {}
        return list;
    }
}
