package com.hostiflix.webservice.githubWs

import com.hostiflix.config.GithubConfig
import com.hostiflix.dto.*
import com.hostiflix.entity.Project
import com.hostiflix.support.BadRequestException
import com.hostiflix.support.UnprocessableEntityException
import org.springframework.context.annotation.Profile
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

@Service
@Profile("!test")
class GithubWsImpl(
    private val githubConfig: GithubConfig,
    private val restTemplate: RestTemplate
): GithubWs {

    override fun getAccessToken(code: String, state: String) : String {

        val response = restTemplate.exchange(
            githubConfig.loginBase + githubConfig.loginGetAccessToken,
            HttpMethod.POST,
            null,
            GithubAccessTokenDto::class.java,
            code,
            state
        )

        return response.body!!.accessToken
    }

    override fun getCustomer(accessToken: String): GithubCustomerDto {
        val headers = HttpHeaders()
        headers.setBearerAuth(accessToken)

        val response = restTemplate.exchange(
            githubConfig.apiBase + githubConfig.apiUser,
            HttpMethod.GET,
            HttpEntity<Any>(headers),
            GithubCustomerDto::class.java
        )

        return response.body!!
    }

    override fun getCustomerPrimaryEmail(accessToken: String) : String {
        val headers = HttpHeaders()
        headers.setBearerAuth(accessToken)

        val customerEmails = restTemplate.exchange(
            githubConfig.apiBase + githubConfig.apiEmails,
            HttpMethod.GET,
            HttpEntity<Any>(headers),
            object : ParameterizedTypeReference<List<GithubEmailResponseDto>>() {}
        )

        return customerEmails.body!!.first { it.primary }.email
    }

    override fun createWebhook(accessToken: String, project: Project) {
        val headers = HttpHeaders()
        headers.setBearerAuth(accessToken)

        val httpEntity = HttpEntity<Any>(GithubWebhookRequestDto(githubConfig.webhookEndpoint), headers)

        try {
            restTemplate.exchange(
                githubConfig.apiBase + githubConfig.apiWebhook,
                HttpMethod.POST,
                httpEntity,
                Any::class.java,
                project.repositoryOwner,
                project.repositoryName
            )
        } catch (e: HttpStatusCodeException) {
            val errorMessage = "Github webhook creation failed with status code ${e.statusCode}, body: ${e.responseBodyAsString}"
            when (e.statusCode) {
                HttpStatus.UNPROCESSABLE_ENTITY -> throw UnprocessableEntityException(errorMessage)
                HttpStatus.BAD_REQUEST -> throw BadRequestException(errorMessage)
                else -> throw IllegalArgumentException(errorMessage)
            }
        }
    }

    override fun getAllRepos(accessToken: String) : List<GithubRepoDto> {
        val headers = HttpHeaders()
        headers.setBearerAuth(accessToken)

        val response = restTemplate.exchange(
            githubConfig.apiBase + githubConfig.apiRepos,
            HttpMethod.GET,
            HttpEntity<Any>(headers),
            object : ParameterizedTypeReference<List<GithubRepoDto>>() {}
        )

        return response.body!!
    }

    override fun getAllBranches(accessToken: String, repoOwner: String, repoName : String) : List<GithubBranchDto> {
        val headers = HttpHeaders()
        headers.setBearerAuth(accessToken)

        val response = restTemplate.exchange(
            githubConfig.apiBase + githubConfig.apiBranches,
            HttpMethod.GET,
            HttpEntity<Any>(headers),
            object : ParameterizedTypeReference<List<GithubBranchDto>>() {},
            repoOwner,
            repoName
        )

        return response.body!!
    }
}