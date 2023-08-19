package application;

import framework.FWContext;

public class Application {

	public static void main(String[] args) {
		FWContext fWContext = new FWContext();
		fWContext.readServiceClasses();

		BankService bankService =  (BankService) fWContext.getServiceBeanOftype(BankService.class);
		if (bankService != null)
			bankService.deposit();
	}
}
