package ru.gumerbaev.family.account.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.gumerbaev.family.account.domain.Account
import ru.gumerbaev.family.account.domain.User
import ru.gumerbaev.family.account.service.AccountService
import java.security.Principal
import javax.validation.Valid

@RestController
class AccountController {

    @Autowired
    private lateinit var accountService: AccountService

    @PreAuthorize("#oauth2.hasScope('server')")  /*or #name.equals('demo')*/
    @RequestMapping(path = arrayOf("/{name}"), method = arrayOf(RequestMethod.GET))
    fun getAccountByName(@PathVariable name: String): Account {
        return accountService.findByName(name)
    }

    @RequestMapping(path = arrayOf("/current"), method = arrayOf(RequestMethod.GET))
    fun getCurrentAccount(principal: Principal): Account {
        return accountService.findByName(principal.name)
    }

    @RequestMapping(path = arrayOf("/current"), method = arrayOf(RequestMethod.PUT))
    fun saveCurrentAccount(principal: Principal, @Valid @RequestBody account: Account) {
        accountService.saveChanges(principal.name, account)
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun createNewAccount(@Valid @RequestBody user: User): Account {
        return accountService.create(user)
    }
}