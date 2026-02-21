// InvoiceRepository.java
package com.firstVersion.RevPay.repository;

import com.firstVersion.RevPay.entity.Invoice;
import com.firstVersion.RevPay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByBusiness(User business);
    List<Invoice> findByCustomerEmail(String email);
}