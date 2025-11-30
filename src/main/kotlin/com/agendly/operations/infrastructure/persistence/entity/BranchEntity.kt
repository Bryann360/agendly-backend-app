package com.agendly.operations.infrastructure.persistence.entity

import com.agendly.shared.persistence.BaseJpaEntity
import com.agendly.tenant.infrastructure.persistence.entity.TenantEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(
  name = "branches",
  indexes = [Index(name = "idx_branches_tenant_id", columnList = "tenant_id")]
)
class BranchEntity : BaseJpaEntity() {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  lateinit var tenant: TenantEntity

  @Column(nullable = false, length = 150)
  lateinit var name: String

  @Column(name = "address_street")
  var addressStreet: String? = null

  @Column(name = "address_number")
  var addressNumber: String? = null

  @Column(name = "address_complement")
  var addressComplement: String? = null

  @Column(name = "address_neighborhood")
  var addressNeighborhood: String? = null

  @Column(name = "address_city")
  var addressCity: String? = null

  @Column(name = "address_state", length = 2)
  var addressState: String? = null

  @Column(name = "address_zip_code")
  var addressZipCode: String? = null

  @Column(name = "address_country")
  var addressCountry: String? = "BR"

  @Column(name = "address_extra", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  var addressExtra: MutableMap<String, Any?>? = null

  @Column(nullable = false, length = 60)
  var timezone: String = "America/Sao_Paulo"

  @Column(nullable = false)
  var active: Boolean = true
}
