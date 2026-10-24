package com.example.data

/**
 * VERIFIED COMPANY REFERENCE DATA
 *
 * This structured knowledge base serves as the single source of truth for the
 * Governance Engine and Resolution Bot. Information is never treated as verified
 * unless explicitly present in this reference store.
 */
data class ApprovedFact(
  val topic: String,
  val statement: String,
  val sourcePolicy: String
)

data class CompanyProfile(
  val companyName: String = "Acme Corporation / Production",
  val industry: String = "Enterprise Cloud Software & E-Commerce Infrastructure",
  val policies: List<String> = listOf(
    "POL-01: Zero-knowledge disclosure of customer order details without prior authenticated identity verification.",
    "POL-02: Support bots must never quote unapproved discounts or pricing terms exceeding standard tier limits.",
    "POL-03: Candidate recruitment information must never request protected personal characteristics or make binding employment guarantees.",
    "POL-04: Customer data exports require cryptographic identity token validation via the customer security portal."
  ),
  val privacyRules: List<String> = listOf(
    "PII-01: Redact credit card numbers, CVVs, and social security numbers prior to message delivery.",
    "PII-02: User addresses, phone numbers, and full names are restricted to verified authenticated sessions.",
    "PII-03: Cross-customer data leakage is strictly prohibited. An agent must never cite data belonging to another account.",
    "PII-04: Disclosing shipping addresses or order values to unverified email queries triggers immediate block and escalation."
  ),
  val dataProtectionRules: List<String> = listOf(
    "DP-01: Database credentials, API tokens, and secret keys must never be echoed in bot outputs.",
    "DP-02: Responses containing sensitive attribute inferences must be held for human review.",
    "DP-03: Order itemization must be suppressed unless authorized session token is active."
  ),
  val recruitmentRules: List<String> = listOf(
    "REC-01: AI assistants cannot extend formal employment offers or state unlisted compensation brackets.",
    "REC-02: Inquiries about open positions must direct applicants to https://careers.acme.example.",
    "REC-03: No evaluation of candidate qualifications may be provided directly by conversational bots."
  ),
  val supportRules: List<String> = listOf(
    "SUP-01: Maximum automated refund authorization limit is $0. All refunds require human billing review.",
    "SUP-02: Account access issues must direct user to sign in through the secure portal at https://auth.acme.example.",
    "SUP-03: Service Level Agreement (SLA) response time is 15 minutes for critical incidents."
  ),
  val approvedFacts: List<ApprovedFact> = listOf(
    ApprovedFact(
      topic = "Order Verification",
      statement = "Customer order details may only be shared through the authenticated portal: https://auth.acme.example/orders.",
      sourcePolicy = "PII-04"
    ),
    ApprovedFact(
      topic = "Return Window",
      statement = "Standard hardware return window is 30 days from physical delivery.",
      sourcePolicy = "SUP-01"
    ),
    ApprovedFact(
      topic = "Support Hours",
      statement = "Live enterprise support operates 24/7 with human supervisors on shift.",
      sourcePolicy = "SUP-03"
    ),
    ApprovedFact(
      topic = "Engineering Careers",
      statement = "All open engineering roles and hiring inquiries are handled exclusively at careers.acme.example.",
      sourcePolicy = "REC-02"
    )
  )
)

val VerifiedCompanyKnowledge = CompanyProfile()
