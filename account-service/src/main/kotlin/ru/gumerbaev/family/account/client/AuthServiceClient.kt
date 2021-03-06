package ru.gumerbaev.family.account.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import ru.gumerbaev.family.account.domain.User

@FeignClient("auth-service")
interface AuthServiceClient {

    @RequestMapping(method = [RequestMethod.POST], path = ["/uaa/users"], consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun createUser(user: User)

    @RequestMapping(method = [RequestMethod.DELETE], path = ["/uaa/users"], consumes = [MediaType.TEXT_PLAIN_VALUE])
    fun deleteUser(username: String)
}
