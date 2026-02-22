package com.revpay.dto.analytics;

import java.math.BigDecimal;
import java.util.Map;

public class AnalyticsDto {

    public static class Summary {
        private BigDecimal totalRevenue;
        private BigDecimal revenueThisMonth;
        private BigDecimal revenueLastMonth;
        private BigDecimal revenueGrowthPercent;
        private long totalInvoices;
        private long paidInvoices;
        private long unpaidInvoices;
        private long draftInvoices;
        private BigDecimal outstandingReceivables;
        private long totalLoans;
        private long activeLoans;
        private BigDecimal loanOutstanding;
        private BigDecimal averageInvoiceValue;
        private double invoicePaymentRate;

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public BigDecimal getRevenueThisMonth() { return revenueThisMonth; }
        public BigDecimal getRevenueLastMonth() { return revenueLastMonth; }
        public BigDecimal getRevenueGrowthPercent() { return revenueGrowthPercent; }
        public long getTotalInvoices() { return totalInvoices; }
        public long getPaidInvoices() { return paidInvoices; }
        public long getUnpaidInvoices() { return unpaidInvoices; }
        public long getDraftInvoices() { return draftInvoices; }
        public BigDecimal getOutstandingReceivables() { return outstandingReceivables; }
        public long getTotalLoans() { return totalLoans; }
        public long getActiveLoans() { return activeLoans; }
        public BigDecimal getLoanOutstanding() { return loanOutstanding; }
        public BigDecimal getAverageInvoiceValue() { return averageInvoiceValue; }
        public double getInvoicePaymentRate() { return invoicePaymentRate; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final Summary s = new Summary();
            public Builder totalRevenue(BigDecimal v) { s.totalRevenue = v; return this; }
            public Builder revenueThisMonth(BigDecimal v) { s.revenueThisMonth = v; return this; }
            public Builder revenueLastMonth(BigDecimal v) { s.revenueLastMonth = v; return this; }
            public Builder revenueGrowthPercent(BigDecimal v) { s.revenueGrowthPercent = v; return this; }
            public Builder totalInvoices(long v) { s.totalInvoices = v; return this; }
            public Builder paidInvoices(long v) { s.paidInvoices = v; return this; }
            public Builder unpaidInvoices(long v) { s.unpaidInvoices = v; return this; }
            public Builder draftInvoices(long v) { s.draftInvoices = v; return this; }
            public Builder outstandingReceivables(BigDecimal v) { s.outstandingReceivables = v; return this; }
            public Builder totalLoans(long v) { s.totalLoans = v; return this; }
            public Builder activeLoans(long v) { s.activeLoans = v; return this; }
            public Builder loanOutstanding(BigDecimal v) { s.loanOutstanding = v; return this; }
            public Builder averageInvoiceValue(BigDecimal v) { s.averageInvoiceValue = v; return this; }
            public Builder invoicePaymentRate(double v) { s.invoicePaymentRate = v; return this; }
            public Summary build() { return s; }
        }
    }

    public static class RevenuePoint {
        private String period;
        private BigDecimal revenue;
        private long invoiceCount;

        public String getPeriod() { return period; }
        public BigDecimal getRevenue() { return revenue; }
        public long getInvoiceCount() { return invoiceCount; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final RevenuePoint r = new RevenuePoint();
            public Builder period(String v) { r.period = v; return this; }
            public Builder revenue(BigDecimal v) { r.revenue = v; return this; }
            public Builder invoiceCount(long v) { r.invoiceCount = v; return this; }
            public RevenuePoint build() { return r; }
        }
    }

    public static class TopCustomer {
        private String customerName;
        private String customerEmail;
        private long invoiceCount;
        private BigDecimal totalPaid;

        public String getCustomerName() { return customerName; }
        public String getCustomerEmail() { return customerEmail; }
        public long getInvoiceCount() { return invoiceCount; }
        public BigDecimal getTotalPaid() { return totalPaid; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final TopCustomer t = new TopCustomer();
            public Builder customerName(String v) { t.customerName = v; return this; }
            public Builder customerEmail(String v) { t.customerEmail = v; return this; }
            public Builder invoiceCount(long v) { t.invoiceCount = v; return this; }
            public Builder totalPaid(BigDecimal v) { t.totalPaid = v; return this; }
            public TopCustomer build() { return t; }
        }
    }

    public static class InvoiceSummary {
        private Map<String, Long> countByStatus;
        private Map<String, BigDecimal> amountByStatus;
        private BigDecimal totalOverdue;
        private long overdueCount;
        private BigDecimal averageDaysToPayment;

        public Map<String, Long> getCountByStatus() { return countByStatus; }
        public Map<String, BigDecimal> getAmountByStatus() { return amountByStatus; }
        public BigDecimal getTotalOverdue() { return totalOverdue; }
        public long getOverdueCount() { return overdueCount; }
        public BigDecimal getAverageDaysToPayment() { return averageDaysToPayment; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final InvoiceSummary s = new InvoiceSummary();
            public Builder countByStatus(Map<String, Long> v) { s.countByStatus = v; return this; }
            public Builder amountByStatus(Map<String, BigDecimal> v) { s.amountByStatus = v; return this; }
            public Builder totalOverdue(BigDecimal v) { s.totalOverdue = v; return this; }
            public Builder overdueCount(long v) { s.overdueCount = v; return this; }
            public Builder averageDaysToPayment(BigDecimal v) { s.averageDaysToPayment = v; return this; }
            public InvoiceSummary build() { return s; }
        }
    }
}