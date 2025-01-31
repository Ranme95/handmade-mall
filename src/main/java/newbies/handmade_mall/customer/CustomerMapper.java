package newbies.handmade_mall.customer;

import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerDto customerDto) {
        Customer customer = new Customer();

        customer.setAccountId(customerDto.getAccountId());
        customer.setPassword(customerDto.getPassword());
        customer.setName(customerDto.getName());
        customer.setEmail(customerDto.getEmail());
        customer.setPhoneNumber(customerDto.getPhoneNumber());
        customer.setAccountState(AccountState.ACTIVE); //가입할 때 계정상태 'ACTIVE'로 고정

        return customer;
    }

    public CustomerDto toCustomerDto(Customer customer) {

        return CustomerDto.builder()
                          .id(customer.getId())
                          .phoneNumber(customer.getPhoneNumber())
                          .accountId(customer.getAccountId())
                          .email(customer.getEmail())
                          .name(customer.getName())
                          .password(customer.getPassword())
                          .build();

    }
}
