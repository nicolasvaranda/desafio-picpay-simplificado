package br.com.picpay_desafio_backend.transaction;

import br.com.picpay_desafio_backend.exception.InvalidTransactionException;
import br.com.picpay_desafio_backend.wallet.Wallet;
import br.com.picpay_desafio_backend.wallet.WalletRepository;
import br.com.picpay_desafio_backend.wallet.WalletType;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;


    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    public Transaction create(Transaction transaction) {
        // 1 - validar transação com base nas regras de négocios
        validateTransaction(transaction);

        // 2 - criar a transação
        var newTransaction = transactionRepository.save(transaction);

        // 3 - debitar o saldo das carteiras
        var payerWallet = walletRepository.findById(transaction.payer()).get();
        var payeeWallet = walletRepository.findById(transaction.payee()).get();
        walletRepository.save(payerWallet.debit(transaction.value()));
        walletRepository.save(payeeWallet.credit(transaction.value()));

        // 4 - chamar serviços externos
        // authorize transaction
        

        return newTransaction;
    }

    /*
     * payer has a common wallet
     * payer has enough balance
     * payer is not the same as payee
     */
    private void validateTransaction(Transaction transaction) {
        walletRepository.findById(transaction.payee())
                .map(payee -> walletRepository.findById(transaction.payer())
                        .map(payer -> isTransactionValid(transaction, payer) ? transaction : null)
                        .orElseThrow(() -> new InvalidTransactionException("Invalid transaction - %s".formatted(transaction))))
                .orElseThrow(
                        () -> new InvalidTransactionException("Invalid transaction - %s".formatted(transaction)));
    }

    private boolean isTransactionValid(Transaction transaction, Wallet payer) {
        return payer.type() == WalletType.COMUM.getValue() &&
                payer.balance().compareTo(transaction.value()) >= 0 &&
                !payer.id().equals(transaction.payee());
    }
}
