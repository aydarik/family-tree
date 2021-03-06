package ru.gumerbaev.family.account.service.security

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.client.OAuth2RestOperations
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices
import java.util.*

/**
 * Extended implementation of [org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices]
 *
 * By default, it designed to return only user details. This class provides [.getRequest] method, which
 * returns clientId and scope of calling service. This information used in controller's security checks.
 */

class CustomUserInfoTokenServices(private val userInfoEndpointUrl: String, private val clientId: String) : ResourceServerTokenServices {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private val PRINCIPAL_KEYS = arrayOf("user", "username", "userid", "user_id", "login", "id", "name")
    }

    private var restTemplate: OAuth2RestOperations? = null

    private var tokenType = DefaultOAuth2AccessToken.BEARER_TYPE

    private var authoritiesExtractor: AuthoritiesExtractor = FixedAuthoritiesExtractor()

    @Throws(InvalidTokenException::class)
    override fun loadAuthentication(accessToken: String): OAuth2Authentication {
        val map = getMap(userInfoEndpointUrl, accessToken)
        return extractAuthentication(map)
    }

    private fun extractAuthentication(map: Map<String, Any>): OAuth2Authentication {
        val principal = getPrincipal(map)
        val request = getRequest(map)
        val authorities = authoritiesExtractor.extractAuthorities(map)
        val token = UsernamePasswordAuthenticationToken(principal, "N/A", authorities)
        token.details = map
        return OAuth2Authentication(request, token)
    }

    private fun getPrincipal(map: Map<String, Any>): Any? {
        for (key in PRINCIPAL_KEYS) {
            if (map.containsKey(key)) {
                return map[key]
            }
        }
        return "unknown"
    }

    @Suppress("UNCHECKED_CAST")
    private fun getRequest(map: Map<String, Any>): OAuth2Request {
        val request = map["oauth2Request"] as Map<String, Any>

        val clientId = request["clientId"] as String
        val scope = LinkedHashSet(
                if (request.containsKey("scope")) {
                    request["scope"] as Collection<String>
                } else {
                    emptySet()
                }
        )

        return OAuth2Request(null, clientId, null, true, HashSet(scope), null, null, null, null)
    }

    override fun readAccessToken(accessToken: String): OAuth2AccessToken {
        throw UnsupportedOperationException("Not supported: read access token")
    }

    @Suppress("UNCHECKED_CAST")
    private fun getMap(path: String, accessToken: String): Map<String, Any> {
        log.debug("Getting user info from: {}", path)
        try {
            if (restTemplate == null) {
                val resource = BaseOAuth2ProtectedResourceDetails()
                resource.clientId = clientId
                restTemplate = OAuth2RestTemplate(resource)
            }

            val clientContext = restTemplate!!.oAuth2ClientContext
            val existingToken = clientContext.accessToken
            if (existingToken == null || accessToken != existingToken.value) {
                val token = DefaultOAuth2AccessToken(accessToken)
                token.tokenType = tokenType
                clientContext.accessToken = token
            }
            return restTemplate!!.getForEntity(path, Map::class.java).body as Map<String, Any>
        } catch (e: Exception) {
            log.error("Could not fetch user details")
            throw InvalidTokenException(accessToken, e)
        }
    }
}
