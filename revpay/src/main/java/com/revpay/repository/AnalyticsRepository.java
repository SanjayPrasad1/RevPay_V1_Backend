package com.revpay.repository;

import com.revpay.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Invoice, Long> {

    @Query(value = """
        SELECT DATE(i.paid_at) AS period,
               COALESCE(SUM(i.total_amount), 0) AS revenue,
               COUNT(i.id) AS invoice_count
        FROM invoices i
        WHERE i.business_user_id = :userId AND i.status = 'PAID'
          AND i.paid_at >= DATE_SUB(NOW(), INTERVAL :days DAY)
        GROUP BY DATE(i.paid_at)
        ORDER BY period ASC
        """, nativeQuery = true)
    List<Object[]> dailyRevenue(@Param("userId") Long userId, @Param("days") int days);

    @Query(value = """
        SELECT YEARWEEK(i.paid_at, 1) AS period,
               COALESCE(SUM(i.total_amount), 0) AS revenue,
               COUNT(i.id) AS invoice_count
        FROM invoices i
        WHERE i.business_user_id = :userId AND i.status = 'PAID'
          AND i.paid_at >= DATE_SUB(NOW(), INTERVAL :weeks WEEK)
        GROUP BY YEARWEEK(i.paid_at, 1)
        ORDER BY period ASC
        """, nativeQuery = true)
    List<Object[]> weeklyRevenue(@Param("userId") Long userId, @Param("weeks") int weeks);

    @Query(value = """
        SELECT DATE_FORMAT(i.paid_at, '%Y-%m') AS period,
               COALESCE(SUM(i.total_amount), 0) AS revenue,
               COUNT(i.id) AS invoice_count
        FROM invoices i
        WHERE i.business_user_id = :userId AND i.status = 'PAID'
          AND i.paid_at >= DATE_SUB(NOW(), INTERVAL :months MONTH)
        GROUP BY DATE_FORMAT(i.paid_at, '%Y-%m')
        ORDER BY period ASC
        """, nativeQuery = true)
    List<Object[]> monthlyRevenue(@Param("userId") Long userId, @Param("months") int months);

    @Query(value = """
        SELECT i.status,
               COUNT(i.id) AS cnt,
               COALESCE(SUM(i.total_amount), 0) AS total
        FROM invoices i
        WHERE i.business_user_id = :userId
        GROUP BY i.status
        """, nativeQuery = true)
    List<Object[]> invoiceStatusSummary(@Param("userId") Long userId);

    @Query(value = """
        SELECT COALESCE(AVG(DATEDIFF(i.paid_at, i.sent_at)), 0)
        FROM invoices i
        WHERE i.business_user_id = :userId AND i.status = 'PAID' AND i.sent_at IS NOT NULL
        """, nativeQuery = true)
    Double averageDaysToPayment(@Param("userId") Long userId);

    @Query(value = """
        SELECT i.customer_name, i.customer_email,
               COUNT(i.id) AS invoice_count,
               COALESCE(SUM(i.total_amount), 0) AS total_paid
        FROM invoices i
        WHERE i.business_user_id = :userId AND i.status = 'PAID'
        GROUP BY i.customer_name, i.customer_email
        ORDER BY total_paid DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> topCustomers(@Param("userId") Long userId, @Param("limit") int limit);

    @Query(value = "SELECT COALESCE(SUM(i.total_amount), 0) FROM invoices i WHERE i.business_user_id = :userId AND i.status = 'PAID' AND MONTH(i.paid_at) = MONTH(NOW()) AND YEAR(i.paid_at) = YEAR(NOW())", nativeQuery = true)
    BigDecimal revenueThisMonth(@Param("userId") Long userId);

    @Query(value = "SELECT COALESCE(SUM(i.total_amount), 0) FROM invoices i WHERE i.business_user_id = :userId AND i.status = 'PAID' AND MONTH(i.paid_at) = MONTH(DATE_SUB(NOW(), INTERVAL 1 MONTH)) AND YEAR(i.paid_at) = YEAR(DATE_SUB(NOW(), INTERVAL 1 MONTH))", nativeQuery = true)
    BigDecimal revenueLastMonth(@Param("userId") Long userId);

    @Query(value = "SELECT COALESCE(SUM(i.total_amount), 0) FROM invoices i WHERE i.business_user_id = :userId AND i.status NOT IN ('PAID','CANCELLED','DRAFT')", nativeQuery = true)
    BigDecimal outstandingReceivables(@Param("userId") Long userId);
}