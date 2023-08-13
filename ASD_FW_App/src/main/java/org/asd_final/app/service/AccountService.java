package org.asd_final.app.service;

import org.asd_final.Autowired;
import org.asd_final.Service;
import org.asd_final.app.dao.AccountDAO;
import org.asd_final.app.dao.IAccountDAO;
import org.asd_final.app.domain.Account;
import org.asd_final.app.domain.Customer;

import java.util.Collection;

@Service
public class AccountService implements IAccountService {
//	private IAccountDAO accountDAO=new AccountDAO();

//	private IEmailSender emailSender;

	@Autowired
	IAccountDAO accountDAO;

	@Autowired
	public AccountService(){
	}

	public Account createAccount(long accountNumber, String customerName) {
		Account account = new Account(accountNumber);
		Customer customer = new Customer(customerName);
		account.setCustomer(customer);
		accountDAO.saveAccount(account);
		//emailSender.sendEmail();
		return account;
	}

	public void deposit(long accountNumber, double amount) {
		Account account = accountDAO.loadAccount(accountNumber);
		account.deposit(amount);
		accountDAO.updateAccount(account);
	}

	public Account getAccount(long accountNumber) {
		Account account = accountDAO.loadAccount(accountNumber);
		return account;
	}

	public Collection<Account> getAllAccounts() {
		return accountDAO.getAccounts();
	}

	public void withdraw(long accountNumber, double amount) {
		Account account = accountDAO.loadAccount(accountNumber);
		account.withdraw(amount);
		accountDAO.updateAccount(account);
	}



	public void transferFunds(long fromAccountNumber, long toAccountNumber, double amount, String description) {
		Account fromAccount = accountDAO.loadAccount(fromAccountNumber);
		Account toAccount = accountDAO.loadAccount(toAccountNumber);
		fromAccount.transferFunds(toAccount, amount, description);
		accountDAO.updateAccount(fromAccount);
		accountDAO.updateAccount(toAccount);
	}
}
