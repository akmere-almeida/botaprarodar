package app.igormatos.botaprarodar.domain.model.community

class CommunityMapper {

    fun mapCommunityResponseToCommunity(communityRequest: List<CommunityRequest>) =
        communityRequest.map {
            Community(
                name = it.name ?: "",
                address = it.address ?: "",
                description = it.description ?: "",
                org_name = it.orgName ?: "",
                org_email = it.orgEmail ?: "",
                id = it.id ?: ""
            )
        }
}