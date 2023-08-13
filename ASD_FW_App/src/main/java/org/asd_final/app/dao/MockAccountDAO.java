package org.asd_final.app.dao;

import org.asd_final.Profile;
import org.asd_final.Service;
import org.asd_final.app.domain.Account;

import java.util.ArrayList;
import java.util.Collection;

@Service
@Profile("dev")
public class MockAccountDAO implements IAccountDAO {
    Collection<Account> accountlist = new ArrayList<Account>();

    public void saveAccount(Account account) {
        accountlist.add(account); // add the new
    }

    public void updateAccount(Account account) {
        Account accountexist = loadAccount(account.getAccountnumber());
        if (accountexist != null) {
            accountlist.remove(accountexist); // remove the old
            accountlist.add(account); // add the new
        }

    }

    public Account loadAccount(long accountnumber) {
        for (Account account : accountlist) {
            if (account.getAccountnumber() == accountnumber) {
                return account;
            }
        }
        return null;
    }

    public Collection<Account> getAccounts() {
        return accountlist;
    }

}