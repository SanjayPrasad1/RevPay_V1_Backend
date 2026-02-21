// TransactionRepository.java
package com.firstVersion.RevPay.repository;

import com.firstVersion.RevPay.entity.Transaction;
import com.firstVersion.RevPay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderOrderByTimestampDesc(User sender);
    List<Transaction> findByReceiverOrderByTimestampDesc(User receiver);
    List<Transaction> findBySenderEmailOrReceiverEmail(String senderEmail, String receiverEmail);
}
