package com.hostiflix

import com.hostiflix.config.GithubConfig
import com.hostiflix.dto.GithubAccessTokenDto
import com.hostiflix.dto.GithubCustomerDto
import com.hostiflix.dto.GithubEmailResponseDto
import com.hostiflix.webservices.GithubWs
import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.boot.test.context.SpringBootTest
import org.junit.Before
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat
import org.springframework.core.ParameterizedTypeReference

@RunWith(MockitoJUnitRunner::class)
@SpringBootTest
class GithubWsTest {

    @Mock
    private lateinit var restTemplate: RestTemplate

    @Mock
    private lateinit var githubConfig: GithubConfig

    @InjectMocks
    private lateinit var githubWs: GithubWs

    @Before
    fun setup() {
        given(githubConfig.loginBase).willReturn("http://github.com/")
        given(githubConfig.loginGetAccessToken).willReturn("access_token?code={code}&state={state}")
        given(githubConfig.apiBase).willReturn("https://api.github.com/")
        given(githubConfig.apiUser).willReturn("user")
        given(githubConfig.apiEmails).willReturn("user/emails")
    }

    @Test
    fun `should return access token`() {
        /* Given */
        val code = "code"
        val state = "state"
        val accessToken = "accessToken"
        val response = ResponseEntity(GithubAccessTokenDto(accessToken), HttpStatus.OK)

        given(restTemplate.exchange(
            "http://github.com/access_token?code=code&state=state",
            HttpMethod.POST,
            null,
            GithubAccessTokenDto::class.java
        )).willReturn(response)

        /* When */
        val accessTokenResult = githubWs.getAccessToken(code, state)

        /* Then */
        assertThat(accessTokenResult).isEqualTo(accessToken)
    }

    @Test
    fun `should return customer`() {
        /* Given */
        val accessToken = "accessToken"
        val githubCustomer = GithubCustomerDto(
            "id1",
            "name1",
            "login1"
        )
        val response = ResponseEntity(githubCustomer, HttpStatus.OK)

        given(restTemplate.exchange(
            eq("https://api.github.com/user"),
            eq(HttpMethod.GET),
            check {
                assertThat(it.headers["Authorization"]).isEqualTo(listOf("Bearer $accessToken"))
            },
            eq(GithubCustomerDto::class.java)
        )).willReturn(response)

        /* When */
        val customerResult = githubWs.getCustomer(accessToken)

        /* Then */
        assertThat(customerResult).isEqualTo(githubCustomer)
    }

    @Test
    fun `should return customer primary email address`() {
        /* Given */
        val accessToken = "accessToken"
        val githubEmail1 = GithubEmailResponseDto().apply { email = "email1" }
        val githubEmail2 = GithubEmailResponseDto().apply { email = "email2" }.apply { primary = true }
        val listOfGithubEmails = listOf(githubEmail1, githubEmail2)
        val response = ResponseEntity(listOfGithubEmails, HttpStatus.OK)

        given(restTemplate.exchange(
            eq("https://api.github.com/user/emails"),
            eq(HttpMethod.GET),
            check {
                assertThat(it.headers["Authorization"]).isEqualTo(listOf("Bearer $accessToken"))
            },
            eq(object : ParameterizedTypeReference<List<GithubEmailResponseDto>>() {})
        )).willReturn(response)

        /* When */
        val emailResult = githubWs.getCustomerPrimaryEmail(accessToken)

        /* Then */
        assertThat(emailResult).isEqualTo(listOfGithubEmails[1].email)
    }
}