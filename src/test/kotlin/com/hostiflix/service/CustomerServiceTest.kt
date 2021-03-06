package com.hostiflix.service

import com.hostiflix.repository.CustomerRepository
import com.hostiflix.support.MockData
import com.nhaarman.mockito_kotlin.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.util.*

@RunWith(SpringJUnit4ClassRunner::class)
class CustomerServiceTest {

    @Mock
    private lateinit var customerRepository: CustomerRepository

    @InjectMocks
    private lateinit var customerService: CustomerService

    @Test
    fun `should return customer by id`() {
        /* Given */
        val id = "id"
        val customer = MockData.customer("1")
        given(customerRepository.findById(id)).willReturn(Optional.of(customer))

        /* When */
        val returnedCustomer = customerService.findCustomerById(id)

        /* Then */
        assertThat(returnedCustomer).isEqualTo(customer)
    }

    @Test
    fun `should return null when customer id is not found`() {
        /* Given */
        val id = "id"
        given(customerRepository.findById(id)).willReturn(Optional.empty())

        /* When */
        val returnedCustomer = customerService.findCustomerById(id)

        /* Then */
        assertThat(returnedCustomer).isNull()
    }
}