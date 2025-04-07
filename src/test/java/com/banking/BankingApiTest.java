package com.banking;

import com.banking.model.Account;
import com.banking.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BankingApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testCreateAccount() {
        // Test valid account creation
        ResponseEntity<Account> response = createAccount("Test User", new BigDecimal("1000.00"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test User", response.getBody().getAccountHolderName());
        assertEquals(new BigDecimal("1000.00"), response.getBody().getBalance());

        // Test negative balance
        ResponseEntity<Account> negativeBalanceResponse = createAccount("Test User 2", new BigDecimal("-100.00"));
        assertEquals(HttpStatus.BAD_REQUEST, negativeBalanceResponse.getStatusCode());
    }

    @Test
    public void testGetAccount() {
        // Create an account first
        Account createdAccount = createAccount("Test User", new BigDecimal("1000.00")).getBody();
        assertNotNull(createdAccount);

        // Test getting the account
        ResponseEntity<Account> response = restTemplate.getForEntity(
                "/api/accounts/" + createdAccount.getAccountNumber(),
                Account.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdAccount.getAccountNumber(), response.getBody().getAccountNumber());

        // Test getting non-existent account
        ResponseEntity<Account> notFoundResponse = restTemplate.getForEntity(
                "/api/accounts/non-existent",
                Account.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, notFoundResponse.getStatusCode());
    }

    @Test
    public void testTransfer() {
        // Create source and destination accounts
        Account sourceAccount = createAccount("Source User", new BigDecimal("1000.00")).getBody();
        Account destAccount = createAccount("Dest User", new BigDecimal("500.00")).getBody();
        assertNotNull(sourceAccount);
        assertNotNull(destAccount);

        // Test valid transfer
        ResponseEntity<Transaction> transferResponse = restTemplate.postForEntity(
                "/api/accounts/transfer",
                new TransferRequest(
                        sourceAccount.getAccountNumber(),
                        destAccount.getAccountNumber(),
                        new BigDecimal("100.00")
                ),
                Transaction.class
        );
        assertEquals(HttpStatus.OK, transferResponse.getStatusCode());

        // Verify balances
        Account updatedSource = restTemplate.getForEntity(
                "/api/accounts/" + sourceAccount.getAccountNumber(),
                Account.class
        ).getBody();
        Account updatedDest = restTemplate.getForEntity(
                "/api/accounts/" + destAccount.getAccountNumber(),
                Account.class
        ).getBody();

        assertEquals(new BigDecimal("900.00"), updatedSource.getBalance());
        assertEquals(new BigDecimal("600.00"), updatedDest.getBalance());

        // Test insufficient funds
        ResponseEntity<Transaction> insufficientFundsResponse = restTemplate.postForEntity(
                "/api/accounts/transfer",
                new TransferRequest(
                        sourceAccount.getAccountNumber(),
                        destAccount.getAccountNumber(),
                        new BigDecimal("1000.00")
                ),
                Transaction.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, insufficientFundsResponse.getStatusCode());
    }

    @Test
    public void testTransactionHistory() {
        // Create accounts and perform transfer
        Account sourceAccount = createAccount("Source User", new BigDecimal("1000.00")).getBody();
        Account destAccount = createAccount("Dest User", new BigDecimal("500.00")).getBody();
        assertNotNull(sourceAccount);
        assertNotNull(destAccount);

        // Perform transfer
        restTemplate.postForEntity(
                "/api/accounts/transfer",
                new TransferRequest(
                        sourceAccount.getAccountNumber(),
                        destAccount.getAccountNumber(),
                        new BigDecimal("100.00")
                ),
                Transaction.class
        );

        // Test transaction history
        ResponseEntity<List<Transaction>> historyResponse = restTemplate.exchange(
                "/api/accounts/" + sourceAccount.getAccountNumber() + "/transactions",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Transaction>>() {}
        );
        assertEquals(HttpStatus.OK, historyResponse.getStatusCode());
        assertNotNull(historyResponse.getBody());
        assertFalse(historyResponse.getBody().isEmpty());
    }

    private ResponseEntity<Account> createAccount(String name, BigDecimal balance) {
        return restTemplate.postForEntity(
                "/api/accounts",
                new CreateAccountRequest(name, balance),
                Account.class
        );
    }

    private static class CreateAccountRequest {
        private final String accountHolderName;
        private final BigDecimal initialBalance;

        public CreateAccountRequest(String accountHolderName, BigDecimal initialBalance) {
            this.accountHolderName = accountHolderName;
            this.initialBalance = initialBalance;
        }

        public String getAccountHolderName() {
            return accountHolderName;
        }

        public BigDecimal getInitialBalance() {
            return initialBalance;
        }
    }

    private static class TransferRequest {
        private final String sourceAccountNumber;
        private final String destinationAccountNumber;
        private final BigDecimal amount;

        public TransferRequest(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount) {
            this.sourceAccountNumber = sourceAccountNumber;
            this.destinationAccountNumber = destinationAccountNumber;
            this.amount = amount;
        }

        public String getSourceAccountNumber() {
            return sourceAccountNumber;
        }

        public String getDestinationAccountNumber() {
            return destinationAccountNumber;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }
} 