package jp.vehicle.inspection.service;

import jp.vehicle.inspection.audit.AuditService;
import jp.vehicle.inspection.domain.entity.Customer;
import jp.vehicle.inspection.domain.repository.CustomerRepository;
import jp.vehicle.inspection.dto.CustomerRequest;
import jp.vehicle.inspection.exception.BusinessException;
import jp.vehicle.inspection.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Customer not found: " + id));
    }

    @Transactional
    public Customer create(CustomerRequest req) {
        Long userId = currentUserService.getCurrentUserId();
        Customer customer = Customer.builder()
                .customerCode("C-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .name(req.getName())
                .nameKana(req.getNameKana())
                .email(req.getEmail())
                .phone(req.getPhone())
                .postalCode(req.getPostalCode())
                .address(req.getAddress())
                .notes(req.getNotes())
                .consentMarketing(req.isConsentMarketing())
                .build();
        customer.setCreatedBy(userId);
        customer.setUpdatedBy(userId);
        Customer saved = customerRepository.save(customer);
        auditService.log("CREATE", "CUSTOMER", saved.getId(), null, saved);
        return saved;
    }

    @Transactional
    public Customer update(Long id, CustomerRequest req) {
        Customer customer = findById(id);
        Customer before = copy(customer);
        customer.setName(req.getName());
        customer.setNameKana(req.getNameKana());
        customer.setEmail(req.getEmail());
        customer.setPhone(req.getPhone());
        customer.setPostalCode(req.getPostalCode());
        customer.setAddress(req.getAddress());
        customer.setNotes(req.getNotes());
        customer.setConsentMarketing(req.isConsentMarketing());
        customer.setUpdatedBy(currentUserService.getCurrentUserId());
        Customer saved = customerRepository.save(customer);
        auditService.log("UPDATE", "CUSTOMER", id, before, saved);
        return saved;
    }

    private Customer copy(Customer c) {
        return Customer.builder()
                .id(c.getId())
                .customerCode(c.getCustomerCode())
                .name(c.getName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .build();
    }
}
