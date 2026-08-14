package no.fdk.dataservicecatalog.domain

enum class IssueType { WARNING, ERROR }

data class Issue(val type: IssueType, val message: String)
