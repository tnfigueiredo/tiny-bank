package com.tnfigueiredo.tinybank.services

import com.tnfigueiredo.tinybank.exceptions.BusinessRuleValidationException
import com.tnfigueiredo.tinybank.model.Account
import com.tnfigueiredo.tinybank.model.ActivationStatus.DEACTIVATED
import com.tnfigueiredo.tinybank.repositories.AccountRepository
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.*

@SpringBootTest
internal class AccountServiceImplTest {

    private companion object{
        val A_RANDOM_ID: UUID = UUID.fromString("eae467d9-deb2-49b3-aaf5-f1e146e567e1")
        val AN_AGENCY: Short = "1".toShort()
        val AN_AGENCY_AS_STRING = String.format("%04d", AN_AGENCY)
        val AN_YEAR = "2024".toShort()
        val A_LATEST_ACCOUNT = "${AN_YEAR}${AN_AGENCY_AS_STRING}0002"
    }

    @MockitoBean
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var accountService: AccountService

    @Test
    fun `when there is no account for the agency in the current year`() {
        val accountToBeSaved = Account(
            id = "${AN_YEAR}${AN_AGENCY_AS_STRING}0000",
            agency = AN_AGENCY,
            userId = A_RANDOM_ID,
            balance = 0.0,
            year = AN_YEAR
        )

        `when`(accountRepository.findLatestAccount("$AN_YEAR$AN_AGENCY_AS_STRING")).thenReturn(null)
        `when`(accountRepository.getAccountByUserId(A_RANDOM_ID)).thenReturn(Result.success(null))
        `when`(accountRepository.saveAccount(accountToBeSaved)).thenReturn(Result.success(accountToBeSaved))
        val result = accountService.createAccount(A_RANDOM_ID, AN_AGENCY, AN_YEAR).getOrNull()!!

        result shouldBeEqual accountToBeSaved

    }

    @Test
    fun `when there is an account for the agency in the current year`() {
        val accountToBeSaved = Account(
            id = "${AN_YEAR}${AN_AGENCY_AS_STRING}0003",
            agency = AN_AGENCY,
            userId = A_RANDOM_ID,
            balance = 0.0,
            year = AN_YEAR
        )

        `when`(accountRepository.findLatestAccount("$AN_YEAR$AN_AGENCY_AS_STRING")).thenReturn(A_LATEST_ACCOUNT)
        `when`(accountRepository.getAccountByUserId(A_RANDOM_ID)).thenReturn(Result.success(null))
        `when`(accountRepository.saveAccount(accountToBeSaved)).thenReturn(Result.success(accountToBeSaved))
        val result = accountService.createAccount(A_RANDOM_ID, AN_AGENCY, AN_YEAR).getOrNull()!!

        result shouldBeEqual accountToBeSaved
    }

    @Test
    fun `when use user already has an account created`() {
        val accountToBeSaved = Account(
            id = "${AN_YEAR}${AN_AGENCY_AS_STRING}0003",
            agency = AN_AGENCY,
            userId = A_RANDOM_ID,
            balance = 0.0,
            year = AN_YEAR
        )

        `when`(accountRepository.findLatestAccount("$AN_YEAR$AN_AGENCY_AS_STRING")).thenReturn(A_LATEST_ACCOUNT)
        `when`(accountRepository.getAccountByUserId(A_RANDOM_ID)).thenReturn(Result.success(accountToBeSaved))

        accountService.createAccount(A_RANDOM_ID, AN_AGENCY, AN_YEAR).exceptionOrNull()!!.shouldBeInstanceOf<BusinessRuleValidationException>()
    }

    @Test
    fun `when there is an active account being deactivated`() {
        val accountToBeDeactivated = Account(
            id = "${AN_YEAR}${AN_AGENCY_AS_STRING}0003",
            agency = AN_AGENCY,
            userId = A_RANDOM_ID,
            balance = 0.0,
            year = AN_YEAR
        )

        `when`(accountRepository.getAccountById(accountToBeDeactivated.id!!)).thenReturn(Result.success(accountToBeDeactivated))
        `when`(accountRepository.deactivateAccount(accountToBeDeactivated.id!!)).thenReturn(Result.success(accountToBeDeactivated.deactivateAccount()))
        val result = accountService.deactivateAccount(accountToBeDeactivated.id!!).getOrNull()!!

        result shouldBeEqual accountToBeDeactivated.deactivateAccount()
    }

    @Test
    fun `when there is a deactivated account being deactivated`() {
        val accountToBeDeactivated = Account(
            id = "${AN_YEAR}${AN_AGENCY_AS_STRING}0003",
            agency = AN_AGENCY,
            userId = A_RANDOM_ID,
            balance = 0.0,
            year = AN_YEAR,
            status = DEACTIVATED
        )

        `when`(accountRepository.getAccountById(accountToBeDeactivated.id!!)).thenReturn(Result.success(accountToBeDeactivated))
        accountService.deactivateAccount(accountToBeDeactivated.id!!).exceptionOrNull()!!.shouldBeInstanceOf<BusinessRuleValidationException>()
    }

    @Test
    fun `when there is a non zero balance account being deactivated`() {
        val accountToBeDeactivated = Account(
            id = "${AN_YEAR}${AN_AGENCY_AS_STRING}0003",
            agency = AN_AGENCY,
            userId = A_RANDOM_ID,
            balance = 100.0,
            year = AN_YEAR
        )

        `when`(accountRepository.getAccountById(accountToBeDeactivated.id!!)).thenReturn(Result.success(accountToBeDeactivated))
        accountService.deactivateAccount(accountToBeDeactivated.id!!).exceptionOrNull()!!.shouldBeInstanceOf<BusinessRuleValidationException>()
    }

    @Test
    fun `when there is a deactivated account being activated`() {
        val accountToBeActivated = Account(
            id = "${AN_YEAR}${AN_AGENCY_AS_STRING}0003",
            agency = AN_AGENCY,
            userId = A_RANDOM_ID,
            balance = 0.0,
            year = AN_YEAR,
            status = DEACTIVATED
        )

        `when`(accountRepository.getAccountById(accountToBeActivated.id!!)).thenReturn(Result.success(accountToBeActivated))
        `when`(accountRepository.activateAccount(accountToBeActivated.id!!)).thenReturn(Result.success(accountToBeActivated.activateAccount()))
        val result = accountService.activateAccount(accountToBeActivated.id!!).getOrNull()!!

        result shouldBeEqual accountToBeActivated.activateAccount()
    }

    @Test
    fun `when there is an active account being activated`() {
        val accountToBeActivated = Account(
            id = "${AN_YEAR}${AN_AGENCY_AS_STRING}0003",
            agency = AN_AGENCY,
            userId = A_RANDOM_ID,
            balance = 0.0,
            year = AN_YEAR
        )

        `when`(accountRepository.getAccountById(accountToBeActivated.id!!)).thenReturn(Result.success(accountToBeActivated))
        accountService.activateAccount(accountToBeActivated.id!!).exceptionOrNull()!!.shouldBeInstanceOf<BusinessRuleValidationException>()
    }

    @Test
    fun `when an account exists for an user id`() {
        val accountToBeSearched = Account(
            id = "${AN_YEAR}${AN_AGENCY_AS_STRING}0003",
            agency = AN_AGENCY,
            userId = A_RANDOM_ID,
            balance = 0.0,
            year = AN_YEAR
        )

        `when`(accountRepository.getAccountByUserId(A_RANDOM_ID)).thenReturn(Result.success(accountToBeSearched))
        accountService.getAccountByUserId(A_RANDOM_ID).getOrNull()?.shouldBeEqual(accountToBeSearched)
    }

    @Test
    fun `when an account doesn't exist for an user id`() {
        `when`(accountRepository.getAccountByUserId(A_RANDOM_ID)).thenReturn(Result.success(null))
        accountService.getAccountByUserId(A_RANDOM_ID).getOrNull().shouldBeNull()
    }

}