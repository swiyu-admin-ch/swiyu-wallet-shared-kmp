package ch.admin.foitt.swiyu.shared.backup.mock

import ch.admin.foitt.swiyu.shared.backup.model.Credential
import ch.admin.foitt.swiyu.shared.backup.model.Jwk
import ch.admin.foitt.swiyu.shared.backup.model.VC

object ContainerMocks {

    const val JSON_SCHEMA = "jsonSchema"
    const val ISSUER_METADATA = "issuerMetadata"
    const val OCA_BUNDLE = "ocaBundle"

    const val UPDATED_JSON_SCHEMA = "updatedJsonSchema"
    const val UPDATED_ISSUER_METADATA = "updatedIssuerMetadata"
    const val UPDATED_OCA_BUNDLE = "updatedOCABundle"

    val holderBindingJwk = Jwk(
        d = "d",
        x = "x",
        crv = "crv",
        kty = "kty",
    )

    val dpopJwk = Jwk(
        d = "d",
        x = "x",
        crv = "crv",
        kty = "kty",
    )

    val noKeyBindingCredential1 = Credential(
        rawCredential = "jwt",
        jwks = null,
        jsonSchema = JSON_SCHEMA,
        issuerMetadata = ISSUER_METADATA,
        ocaBundle = OCA_BUNDLE,
    )

    val noKeyBindingCredential2 = Credential(
        rawCredential = "jwt",
        jwks = null,
        jsonSchema = JSON_SCHEMA,
        issuerMetadata = ISSUER_METADATA,
        ocaBundle = OCA_BUNDLE,
    )

    val updatedNoKeyBindingCredential = Credential(
        rawCredential = "jwt",
        jwks = null,
        jsonSchema = UPDATED_JSON_SCHEMA,
        issuerMetadata = UPDATED_ISSUER_METADATA,
        ocaBundle = UPDATED_OCA_BUNDLE,
    )

    val hardwareKeyBindingCredential1 = Credential(
        rawCredential = "jwtWithCnf",
        jwks = null,
        jsonSchema = JSON_SCHEMA,
        issuerMetadata = ISSUER_METADATA,
        ocaBundle = OCA_BUNDLE
    )

    val hardwareKeyBindingCredential2 = Credential(
        rawCredential = "jwtWithCnf",
        jwks = null,
        jsonSchema = JSON_SCHEMA,
        issuerMetadata = ISSUER_METADATA,
        ocaBundle = OCA_BUNDLE
    )

    val softwareKeyBindingCredential1 = Credential(
        rawCredential = "jwt",
        jwks = listOf(holderBindingJwk),
        jsonSchema = JSON_SCHEMA,
        issuerMetadata = ISSUER_METADATA,
        ocaBundle = OCA_BUNDLE
    )

    val softwareKeyBindingCredential2 = Credential(
        rawCredential = "jwt",
        jwks = listOf(holderBindingJwk),
        jsonSchema = JSON_SCHEMA,
        issuerMetadata = ISSUER_METADATA,
        ocaBundle = OCA_BUNDLE
    )

    val noOCACredential = noKeyBindingCredential1.copy(
        ocaBundle = null
    )

    // single credentials

    val singleVCNoKeyBinding = VC(
        format = "format",
        credentials = listOf(
            noKeyBindingCredential1,
        ),
        refreshToken = "refreshToken",
        dpopJwk = dpopJwk,
    )

    val singleVCNotRefreshable = singleVCNoKeyBinding.copy(refreshToken = null)

    val singleVCNoDpopNoKeyBinding = singleVCNoKeyBinding.copy(dpopJwk = null)

    val singleVCHardwareKeyBinding = VC(
        format = "format",
        credentials = listOf(
            hardwareKeyBindingCredential1,
        ),
        refreshToken = "refreshToken",
        dpopJwk = dpopJwk,
    )

    val singleVCNoDpopHardwareKeyBinding = singleVCHardwareKeyBinding.copy(dpopJwk = null)

    val singleVCSoftwareKeyBinding = VC(
        format = "format",
        credentials = listOf(
            softwareKeyBindingCredential1,
        ),
        refreshToken = "refreshToken",
        dpopJwk = dpopJwk,
    )

    val singleVCNoDpopSoftwareKeyBinding = singleVCSoftwareKeyBinding.copy(dpopJwk = null)

    // batch credentials

    val batchVCNoKeyBinding = VC(
        format = "format",
        credentials = listOf(
            noKeyBindingCredential1,
            noKeyBindingCredential2,
        ),
        refreshToken = "refreshToken",
        dpopJwk = dpopJwk,
    )

    val batchVCNoDpopNoKeyBinding = batchVCNoKeyBinding.copy(dpopJwk = null)

    val batchVCHardwareKeyBinding = VC(
        format = "format",
        credentials = listOf(
            hardwareKeyBindingCredential1,
            hardwareKeyBindingCredential2,
        ),
        refreshToken = "refreshToken",
        dpopJwk = dpopJwk,
    )

    val batchVCNoDpopHardwareKeyBinding = batchVCHardwareKeyBinding.copy(dpopJwk = null)

    val batchVCSoftwareKeyBinding = VC(
        format = "format",
        credentials = listOf(
            softwareKeyBindingCredential1,
            softwareKeyBindingCredential2,
        ),
        refreshToken = "refreshToken",
        dpopJwk = dpopJwk,
    )

    val batchVCNoDpopSoftwareKeyBinding = batchVCSoftwareKeyBinding.copy(dpopJwk = null)

    val updatedBatchVC = VC(
        format = "format",
        credentials = listOf(
            noKeyBindingCredential1,
            updatedNoKeyBindingCredential,
        ),
        refreshToken = "refreshToken",
        dpopJwk = null
    )
}