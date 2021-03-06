package com.hostiflix.webservice.githubWs

import com.hostiflix.dto.GithubBranchDto
import com.hostiflix.dto.GithubCustomerDto
import com.hostiflix.dto.GithubRepoDto
import com.hostiflix.entity.Project

interface GithubWs {

    fun getAccessToken(code: String, state: String) : String

    fun getCustomer(accessToken: String): GithubCustomerDto

    fun getCustomerPrimaryEmail(accessToken: String) : String

    fun createWebhook(accessToken: String, project: Project)

    fun getAllRepos(accessToken: String) : List<GithubRepoDto>

    fun getAllBranches(accessToken: String, repoOwner: String, repoName : String) : List<GithubBranchDto>
}