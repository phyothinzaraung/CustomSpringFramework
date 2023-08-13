package org.asd_final.app;

import org.asd_final.Service;

@Service
public class CustomerServiceImpl implements CustomerService{

    @Override
    public void print() {
        System.out.println("Hello");
    }
}
