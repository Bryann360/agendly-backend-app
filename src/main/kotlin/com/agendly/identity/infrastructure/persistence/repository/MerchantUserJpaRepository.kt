package com.agendly.identity.infrastructure.persistence.repository

import com.agendly.identity.infrastructure.persistence.entity.MerchantUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MerchantUserJpaRepository : JpaRepository<MerchantUserEntity, UUID> {
  fun findByTenantIdAndEmail(tenantId: UUID, email: String): MerchantUserEntity?
  fun findByEmail(email: String): MerchantUserEntity?
}
